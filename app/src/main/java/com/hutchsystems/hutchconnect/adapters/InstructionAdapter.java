package com.hutchsystems.hutchconnect.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hutchsystems.hutchconnect.R;

/**
 * Created by Deepak Sharma on 3/25/2017.
 */

public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.ViewHolder> {
    String[] data;

    public InstructionAdapter(String[] data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.instruction_row_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String instruction = data[position];
        int serialNo = position + 1;
        holder.tvSerialNo.setText(serialNo + "");
        holder.tvInstructions.setText(instruction);

    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSerialNo, tvInstructions;

        public ViewHolder(View view) {
            super(view);
            tvSerialNo = (TextView) view.findViewById(R.id.tvSerialNo);
            tvInstructions = (TextView) view.findViewById(R.id.tvInstructions);
        }
    }
}
