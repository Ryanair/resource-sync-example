package ryanair.com.resourcesyncexample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import ryanair.com.resourcesyncexample.model.Airport;

public class AirportsAdapter extends RecyclerView.Adapter<AirportsAdapter.ViewHolder> {
    private List<Airport> mDataSet;

    public AirportsAdapter(List<Airport> dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_airport, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Airport airport = mDataSet.get(position);

        holder.airportCode.setText(airport.getCode());
        holder.airportCountry.setText(airport.getCountry());
        holder.airportName.setText(airport.getName());
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public Airport getItem(int position) {
        return mDataSet.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.airport_name)
        public TextView airportName;
        @InjectView(R.id.airport_country)
        public TextView airportCountry;
        @InjectView(R.id.airport_code)
        public TextView airportCode;
        View view;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.inject(this, v);
            view = v;
        }
    }
}
