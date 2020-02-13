package ndds.com.trakidhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ChildHandler {
    private String UID;
    private Context context;
    private SQLiteChildDataHandler childData;
    private StateObject stateObject = new StateObject() {
        @Override
        void onBothConditionStatisfied() {
            //after loading the children
            if (stateObject.getData() != null) {
                int positionOfDefaultPairCode = childData.getPositionOfPairCode((String) stateObject.getData());
                //set first focus element
                ((ChildListAdapter) ((RecyclerView) ((Activity) context).findViewById(R.id.childSelector)).getAdapter())
                        .forceFocusItem(positionOfDefaultPairCode);
            }
        }
    };

    public ChildHandler(String uid, Context context) {
        UID = uid;
        this.context = context;
        childData = new SQLiteChildDataHandler(context);

    }

    /*this method is called every time the app starts and
     * request children from from firebase*/

    private void setDefaultPairCode(String pairCode) {

        HashMap<String, String> defaultDevice = null;
        if (pairCode != null) {
            defaultDevice = new HashMap<>();
            defaultDevice.put("paircode", pairCode);
            defaultDevice.put("userid", UID);
        }
        FirebaseDatabase.getInstance().getReference("defaultDevice/" + UID)
                .setValue(defaultDevice);
    }

    void getChildrenFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("UserDevices/" + UID);
        getOnlineDefaultPairCodeFirstTime();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                childData.truncateTable(); //happens every time when app opens
                HashMap<String, String> childInfo;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    childInfo = (HashMap<String, String>) child.getValue();
                    childData.insertNewChild(
                            childInfo.get("paircode"),
                            childInfo.get("label")
                    );
                }
                onChildrenLoaded();
                stateObject.setA(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //after all the children from firebase to SQLite we get the default device paircode

    }

    private void getOnlineDefaultPairCodeFirstTime() {
        FirebaseDatabase.getInstance().getReference("defaultDevice/" + UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String defaultPairCode;
                if (!dataSnapshot.hasChildren())
                    /*if user has no children added yet then there
                    will be default paircode.*/
                    defaultPairCode = null;
                else
                    defaultPairCode = ((HashMap<String, String>) dataSnapshot.getValue()).get("paircode");
                onDefaultPaircodeChanged(defaultPairCode);
                stateObject.setData(defaultPairCode);
                stateObject.setB(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void changeChild(int position) {
        Cursor c = childData.getCursor();
        c.moveToPosition(position);
        String pairCode = c.getString(0);
        setDefaultPairCode(pairCode);
        onDefaultPaircodeChanged(pairCode);
    }

    public void addChild(final String paircode, final String firstName, final AlertDialog dialog) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("Devices").orderByChild("paircode")
                .equalTo(paircode).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren())
                    Toast.makeText(context, "No such device exist", Toast.LENGTH_SHORT).show();
                else {
                    DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next().child("status");
                    String value = (String) snapshot.getValue();
                    if (value.equals("1"))
                        Toast.makeText(context, "Device is assigned to another parent", Toast.LENGTH_SHORT).show();
                    else {
                        setNewChildForDatabase(paircode, firstName, snapshot.getRef());
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setNewChildForDatabase(String paircode, String firstName, DatabaseReference ref) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("UserDevices/" + UID);
        HashMap<String, Object> newChildData = new HashMap<>();
        newChildData.put("label", firstName);
        newChildData.put("paircode", paircode);
        newChildData.put("userid", UID);
        myRef.child(paircode).setValue(newChildData);

        //setting the new default device in the database.
        setDefaultPairCode(paircode);
        //update status to 1 of the particular device
        ref.setValue("1");
        //insert the new device to SQLite database
        ChildListAdapter listAdapter = (ChildListAdapter) ((RecyclerView) ((Activity) context)
                .findViewById(R.id.childSelector)).getAdapter();
        if (listAdapter != null)
            listAdapter.addChildAndFocus(firstName);
        Toast.makeText(context, "child " + firstName + " added!", Toast.LENGTH_SHORT).show();
        //childData.insertNewChild(paircode,firstName,phoneumber);
        onDefaultPaircodeChanged(paircode);
    }

    void showChildInfo(int position) {
        Cursor c = childData.getCursor();
        c.moveToPosition(position);
        final String name, paircode;
        String info =
                "Name: " + (name = c.getString(1)) +
                        "\nPair Code: " + (paircode = c.getString(0));

        new AlertDialog.Builder(context).
                setTitle("Child Information").
                setMessage(info).setNegativeButton("Delete child", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteChild(1, paircode, name);
                dialog.dismiss();
            }
        }).show();
    }

    public void deleteChild(int step, final String paircode, final String name) {
        if (step == 1)
            new AlertDialog.Builder(context).setMessage("Are you sure you want to delete " + name + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteChild(2, paircode, name);
                        }
                    }).show();
        if (step == 2) {
            FirebaseDatabase.getInstance().getReference("UserDevices/" + UID + "/" + paircode).removeValue();
            //setting status code back to 0.
            FirebaseDatabase.getInstance().getReference("Devices").orderByChild("paircode")
                    .equalTo(paircode).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dataSnapshot.getChildren().iterator().next().child("status").getRef().setValue("0");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //removing data from sqlite database
            int removedPosition = childData.getPositionOfPairCode(paircode);
            childData.deletePaircode(paircode);
            new SQLIteLocationDataHandler(context).deletePaircode(paircode);
            ChildListAdapter listAdapter = (ChildListAdapter) ((RecyclerView) ((Activity) context)
                    .findViewById(R.id.childSelector)).getAdapter();
            if (listAdapter != null)
                //remove the child from list on child selector
                listAdapter.removeItemAndFocusFirstItem(removedPosition);
            String firstPairCode = null;
            Cursor c = childData.getCursor();
            if (c.getCount() > 0) {
                c.moveToFirst();
                firstPairCode = c.getString(0);
            }
            //set new default value as the first pair code in firebase
            setDefaultPairCode(firstPairCode);
            //refresh map with new paircode
            onDefaultPaircodeChanged(firstPairCode);
            Toast.makeText(context, "removed child " + name, Toast.LENGTH_SHORT).show();
        }
    }

    public void showAddChildWindow() {
        final ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.add_child_alert, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).setView(viewGroup).create();
        viewGroup.findViewById(R.id.add_new_child_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paircode = ((EditText) viewGroup.findViewById(R.id.new_child_pairCode)).getText().toString();
                String firstName = ((EditText) viewGroup.findViewById(R.id.new_child_firstName)).getText().toString();
                if (paircode.length() == 0 || firstName.length() == 0)
                    Toast.makeText(context, "Some of the fields are empty", Toast.LENGTH_SHORT).show();
                else
                    addChild(paircode, firstName, dialog);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void showNoDefaultChildAlert() {
        new AlertDialog.Builder(context).setView(R.layout.no_default_child_poster).show();
    }

    abstract void onChildrenLoaded();

    abstract void onDefaultPaircodeChanged(String defaultPairCode);
}
