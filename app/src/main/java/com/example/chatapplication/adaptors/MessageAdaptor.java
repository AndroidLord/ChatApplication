package com.example.chatapplication.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ItemSendBinding;
import com.example.chatapplication.databinding.ReceivedItemBinding;
import com.example.chatapplication.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageAdaptor extends RecyclerView.Adapter{

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
        if(viewType == ITEM_SEND){
            view = LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
            return new SendViewHolder(view);
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.received_item,parent,false);
            return new ReceivedViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {

        MessageModel messageModel = messageModelArrayList.get(position);
        if(messageModel.getSenderId().equals(FirebaseAuth.getInstance().getUid())){
            // if the current view and sender is same
            return ITEM_SEND;
        }
        else{
            // if the current view and sender are different
            return ITEM_RECEIVED;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel messageModel = messageModelArrayList.get(position);

        if(holder.getClass() == SendViewHolder.class){

            SendViewHolder sendViewHolder = (SendViewHolder) holder;

            String url = messageModel.getImageUrl();

            if(url != null){
            sendViewHolder.binding.sendImage.setVisibility(View.VISIBLE);
            sendViewHolder.binding.sendMessage.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messageModel.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(sendViewHolder.binding.sendImage);
            }
            else{

                sendViewHolder.binding.sendMessage.setVisibility(View.VISIBLE);
                sendViewHolder.binding.sendImage.setVisibility(View.GONE);
                sendViewHolder.binding.sendMessage.setText(messageModel.getMessage());

            }

            // Message Time
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            String time = sdf.format(new Date(messageModel.getTimeStamp()));
            sendViewHolder.binding.messageTime.setText(time);

        }else{

            ReceivedViewHolder receivedViewHolder = (ReceivedViewHolder) holder;

            String url = messageModel.getImageUrl();
            receivedViewHolder.binding.receivedMessage.setText(messageModel.getMessage());

            if(url != null){
                receivedViewHolder.binding.receivedImage.setVisibility(View.VISIBLE);
                receivedViewHolder.binding.receivedMessage.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messageModel.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(receivedViewHolder.binding.receivedImage);
            }
            else{
                receivedViewHolder.binding.receivedImage.setVisibility(View.GONE);
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

    public class SendViewHolder extends RecyclerView.ViewHolder{

        ItemSendBinding binding;
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemSendBinding.bind(itemView);

        }

    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder{

        ReceivedItemBinding binding;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ReceivedItemBinding.bind(itemView);

        }

    }

}
