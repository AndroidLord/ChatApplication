package com.example.chatapplication.adaptors;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ItemSendBinding;
import com.example.chatapplication.databinding.ReceivedItemBinding;
import com.example.chatapplication.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageAdaptor extends RecyclerView.Adapter {

    public static final int ITEM_RECEIVED = 2;
    public static final int ITEM_SEND = 1;
    private Context context;
    private ArrayList<MessageModel> messageModelArrayList;

    public MessageAdaptor() {
    }

    public MessageAdaptor(Context context, ArrayList<MessageModel> messageModelArrayList) {
        this.context = context;
        this.messageModelArrayList = messageModelArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_SEND) {
            view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SendViewHolder(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.received_item, parent, false);
            return new ReceivedViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {

        MessageModel messageModel = messageModelArrayList.get(position);
        if (messageModel.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            // if the current view and sender is same
            return ITEM_SEND;
        } else {
            // if the current view and sender are different
            return ITEM_RECEIVED;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel messageModel = messageModelArrayList.get(position);

        if (holder.getClass() == SendViewHolder.class) {

            SendViewHolder sendViewHolder = (SendViewHolder) holder;

            String pdfUrl = messageModel.getPdfUrl();
            String url = messageModel.getImageUrl();

            if (url != null) {

                // Hide
                sendViewHolder.binding.pdfImage.setVisibility(View.GONE);
                sendViewHolder.binding.pdfName.setVisibility(View.GONE);
                sendViewHolder.binding.sendMessage.setVisibility(View.GONE);

                // Show
                sendViewHolder.binding.sendImage.setVisibility(View.VISIBLE);

                Glide.with(context)
                        .load(messageModel.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(sendViewHolder.binding.sendImage);
            } else if (pdfUrl != null) {

                // hide
                sendViewHolder.binding.sendImage.setVisibility(View.GONE);
                sendViewHolder.binding.sendMessage.setVisibility(View.GONE);

                // Show
                sendViewHolder.binding.pdfImage.setVisibility(View.VISIBLE);
                sendViewHolder.binding.pdfName.setVisibility(View.VISIBLE);

                sendViewHolder.binding.pdfName.setText(messageModel.getPdfName());
                Glide.with(context)
                        .load(messageModel.getPdfUrl())
                        .placeholder(R.drawable.pdf)
                        .into(sendViewHolder.binding.pdfImage);

                sendViewHolder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = messageModel.getPdfUrl();

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setTitle("Downloading PDF file");
                        request.setDescription("Please wait...");

                        // Set the local destination for the downloaded file
                        String fileName = messageModel.getPdfName();
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                        // Enqueue the download request
                        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        long downloadId = downloadManager.enqueue(request);

                        Uri downloadedFileUri = downloadManager.getUriForDownloadedFile(downloadId);

// Create a File object from the downloaded file Uri
                        File file = new File(downloadedFileUri.getPath());

// Create an intent to open the PDF file with a PDF viewer app
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        context.startActivity(intent);

                    }
                });

            } else {
                // Hide
                sendViewHolder.binding.pdfImage.setVisibility(View.GONE);
                sendViewHolder.binding.pdfName.setVisibility(View.GONE);
                sendViewHolder.binding.sendImage.setVisibility(View.GONE);

                // Show
                sendViewHolder.binding.sendMessage.setVisibility(View.VISIBLE);

                sendViewHolder.binding.sendMessage.setText(messageModel.getMessage());

            }

            // Message Time
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String time = sdf.format(new Date(messageModel.getTimeStamp()));
            sendViewHolder.binding.messageTime.setText(time);

        } else {

            ReceivedViewHolder receivedViewHolder = (ReceivedViewHolder) holder;

            String pdfUrl = messageModel.getPdfUrl();
            String url = messageModel.getImageUrl();


            if (url != null) {

                // Hide

                receivedViewHolder.binding.pdfImage.setVisibility(View.GONE);
                receivedViewHolder.binding.pdfName.setVisibility(View.GONE);
                receivedViewHolder.binding.receivedMessage.setVisibility(View.GONE);

                // Show
                receivedViewHolder.binding.receivedImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageModel.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(receivedViewHolder.binding.receivedImage);
            }
            else if (pdfUrl != null) {

                // hide
                receivedViewHolder.binding.receivedImage.setVisibility(View.GONE);
                receivedViewHolder.binding.receivedMessage.setVisibility(View.GONE);

                // Show
                receivedViewHolder.binding.pdfImage.setVisibility(View.VISIBLE);
                receivedViewHolder.binding.pdfName.setVisibility(View.VISIBLE);

                receivedViewHolder.binding.pdfName.setText(messageModel.getPdfName());
                Glide.with(context)
                        .load(messageModel.getPdfUrl())
                        .placeholder(R.drawable.pdf)
                        .into(receivedViewHolder.binding.pdfImage);

            }
            else {

                // Hide
                receivedViewHolder.binding.pdfImage.setVisibility(View.GONE);
                receivedViewHolder.binding.pdfName.setVisibility(View.GONE);
                receivedViewHolder.binding.receivedImage.setVisibility(View.GONE);

                // Show
                receivedViewHolder.binding.receivedMessage.setVisibility(View.VISIBLE);
                receivedViewHolder.binding.receivedMessage.setText(messageModel.getMessage());

            }

            //Time of Message
            // Message Time
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String time = sdf.format(new Date(messageModel.getTimeStamp()));
            receivedViewHolder.binding.messageTime.setText(time);
        }

    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder {

        ItemSendBinding binding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemSendBinding.bind(itemView);

        }

    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder {

        ReceivedItemBinding binding;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ReceivedItemBinding.bind(itemView);

        }

    }

}
