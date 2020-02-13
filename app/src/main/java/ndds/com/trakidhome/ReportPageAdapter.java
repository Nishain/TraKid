package ndds.com.trakidhome;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;

import androidx.viewpager.widget.PagerAdapter;

public class ReportPageAdapter extends PagerAdapter {
    private ArrayList<String> reportFragments;
    private Context context;

    public ReportPageAdapter(ArrayList<String> reportFragments, Context context) {
        this.reportFragments = reportFragments;
        this.context = context;
    }

    @Override
    public int getCount() {
        return reportFragments.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ViewGroup cardView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.single_report_page, null);
        ((TextView) cardView.findViewById(R.id.single_report_txt)).setText(
                TextUtils.join("\n", reportFragments.subList(position * 3, (position + 1) * 3 > reportFragments.size() ? reportFragments.size() : (position + 1) * 3))
        );
        ((TextView) cardView.findViewById(R.id.report_page_no)).setText(String.valueOf((position + 1) + "/" + reportFragments.size() / 4) + " >");
        container.addView(cardView);
        return cardView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
