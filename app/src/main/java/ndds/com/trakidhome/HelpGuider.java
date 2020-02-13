package ndds.com.trakidhome;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fenchtose.tooltip.Tooltip;

import static com.fenchtose.tooltip.Tooltip.BOTTOM;
import static com.fenchtose.tooltip.Tooltip.LEFT;
import static com.fenchtose.tooltip.Tooltip.RIGHT;
import static com.fenchtose.tooltip.Tooltip.TOP;


public class HelpGuider {

    private TextView helpPopupText;
    private Tooltip.Builder tooltipBuilder;
    int currentTourIndex = 0;
    private MainActivity mainActivity;
    private String[] tour;
    int[] directions = new int[]{
            TOP,
            TOP,
            TOP,
            TOP,
            LEFT,
            BOTTOM, RIGHT, BOTTOM,
            RIGHT
    };
    int[] widgets = new int[]{
            R.id.bottom_options,
            R.id.bottom_options,
            R.id.bottom_options,
            R.id.coordinate_aid,
            Integer.MIN_VALUE,
            R.id.find_child_location,
            R.id.mapLegendContainer,
            R.id.childSelector,
            R.id.nav_view


    };
    private Tooltip tooltip;
    public static boolean isInStanceRunning = false;

    public HelpGuider(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        isInStanceRunning = true;
        ViewGroup contentView;
        tour = mainActivity.getResources().getStringArray(R.array.help_tour);
        helpPopupText = (contentView = (ViewGroup) mainActivity.getLayoutInflater().inflate(R.layout.help_popup, null))
                .findViewById(R.id.help_text);
        contentView.findViewById(R.id.next_guide_pop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTourIndex == (tour.length - 1))
                    ((TextView) v).setText("Finish");
                popHelp();
            }
        });
        tooltipBuilder = new Tooltip.Builder(mainActivity)
                .content(contentView)
                .cancelable(false)
                .into((ViewGroup) mainActivity.getWindow().getDecorView())
                .withTip(new Tooltip.Tip(60, 50, mainActivity.getResources().getColor(R.color.appTheme)));

    }

    public void popHelp() {
        if (tooltip != null)
            tooltip.dismiss();
        if (currentTourIndex == tour.length) {
            isInStanceRunning = false;
            return;
        }
        int direction;
        View anchorView;
        helpPopupText.setText(tour[currentTourIndex]);
        if (widgets[currentTourIndex] == Integer.MIN_VALUE)
            //if the widget supposed to my locationButton
            anchorView = ((View) mainActivity.mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        else
            anchorView = mainActivity.findViewById(widgets[currentTourIndex]);

        if (widgets[currentTourIndex] == R.id.nav_view)
            direction = RIGHT;
        else {
            int[] size = new int[2];
            anchorView.getLocationInWindow(size);
            if (size[1] < (mainActivity.getWindow().getDecorView().getHeight() / 2))
                direction = BOTTOM;
            else
                direction = TOP;
        }
        tooltip = tooltipBuilder.anchor(anchorView, direction).show();
        currentTourIndex++;
        //((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"))
    }
}
