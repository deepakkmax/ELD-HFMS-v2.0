package com.hutchsystems.hutchconnect.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hutchsystems.hutchconnect.R;
import com.hutchsystems.hutchconnect.common.Utility;
import com.hutchsystems.hutchconnect.db.TrainingDocumentBean;

import java.io.File;
import java.util.ArrayList;

import static com.hutchsystems.hutchconnect.common.WebUrl.DOCUMENT_URL;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {

    Context context;
    AlertDialog alertDialog;
    ArrayList<TrainingDocumentBean> list;

    public TrainingAdapter(Context context, ArrayList<TrainingDocumentBean> list, AlertDialog alertDialog) {
        this.context = context;
        this.list = list;
        this.alertDialog = alertDialog;
    }


    @NonNull
    @Override
    public TrainingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TrainingAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.training_document_view, parent, false));
    }

    // Modified By: Deepak Sharma
    // Modified Date: 25 March 2020
    // Purpose: removed static code and static url
    @Override
    public void onBindViewHolder(@NonNull TrainingAdapter.ViewHolder holder, final int position) {

        final TrainingDocumentBean bean = list.get(position);

        holder.tvFileName.setText(bean.getDocumentName());

        final String documentContentType = bean.getDocumentContentType();

        holder.ivImage.setImageResource(getIcon(documentContentType));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String documentType = bean.getDocumentType();
                Intent intent = null;

                switch (documentContentType) {
                    case "application/html":
                        Uri uri = Uri.parse(bean.getDocumentPath());
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(bean.getDocumentPath()));
                        context.startActivity(intent);
                        break;
                    case "application/pdf":
                        String clientPath = Utility.GetDocumentFullPath(documentType, bean.getDocumentPath());
                        File file = new File(clientPath);
                        if (file.exists()) {
                            Utility.openPdf(clientPath);
                        } else {
                            String serverUrl = DOCUMENT_URL + documentType + "/" + bean.getDocumentPath();
                            clientPath = Utility.GetDocumentName(documentType, bean.getDocumentPath());
                            if (mListener != null) {
                                mListener.downloadDocument(serverUrl, clientPath);
                            }
                        }
                        break;
                    case "application/video":

                        Utility.openVideo(bean.getDocumentPath());
                        break;
                }
                alertDialog.dismiss();


            }
        });
    }

    // Created By: Deepak Sharma
    // Created Date: 27 March 2020
    // Purpose: set left icon on the basis of document content type
    private int getIcon(String documentContentType) {
        int icon = R.drawable.ic_pdf;
        switch (documentContentType) {
            case "application/html":
                icon = R.drawable.ic_html;
                break;
            case "application/pdf":
                icon = R.drawable.ic_pdf;

                break;
            case "application/video":
                icon = R.drawable.ic_video;
                break;
        }
        return icon;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;

        ImageView ivImage;
        Button butBack;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvFileName = itemView.findViewById(R.id.tvFileName);

            ivImage = itemView.findViewById(R.id.ivImage);
            butBack = itemView.findViewById(R.id.butBack);
        }
    }

    public static ITrainingDocument mListener;

    public interface ITrainingDocument {
        void downloadDocument(String serverPath, String clientPath);
    }
}
