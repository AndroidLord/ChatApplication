package com.example.chatapplication.adaptors;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.activities.GroupChatActivity;
import com.example.chatapplication.databinding.ConverstaionItemBinding;
import com.example.chatapplication.models.UserModel;
import com.example.chatapplication.utils.Credentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UsersAdaptor extends RecyclerView.Adapter<UsersAdaptor.ViewHolder> {

    private Context context;
    private List<UserModel> userModelList;

    private List<UserModel> selectedUserList = new ArrayList<>();
    private boolean isSelectedMode = false;

    private OnUserItemClickListener listener;


    public UsersAdaptor(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
    }

    public interface OnUserItemClickListener {
        void onItemClick(int position,boolean isSelectedMode);

        void onLongItemClick(int position,boolean isSelectedMode);
    }

    public void updateSelectedUserList(boolean isSelectedMode) {
        this.isSelectedMode = isSelectedMode;
        this.selectedUserList.clear(); // remove this line
        //this.selectedUserList = selectedUserList;
        notifyDataSetChanged();
    }

    public void setOnUserItemClick(OnUserItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UsersAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.converstaion_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdaptor.ViewHolder holder, int position) {

        UserModel userModel = userModelList.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + userModel.getUserId();

        // Setting Time and Last message
        FirebaseDatabase.getInstance()
                .getReference()
                .child(Credentials.DATABASE_REF_CHATS)
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String lastMsg = snapshot.child(Credentials.DATABASE_REF_LAST_MSG).getValue(String.class);
                            long lastMsgTime = snapshot.child(Credentials.DATABASE_REF_LAST_MSG_TIME).getValue(Long.class);

                            holder.binding.lastMessageItem.setText(lastMsg);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.timeItem.setText(dateFormat.format(new Date(lastMsgTime)));
                            holder.binding.timeItem.setVisibility(View.VISIBLE);


                        } else {
                            holder.binding.lastMessageItem.setText("Tap To Chat...");
                            holder.binding.timeItem.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // setting up profile image
        Glide.with(context)
                .load(userModel.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profileImageItem);

//
//        Picasso.get().load(userModel.getProfileImage())
//                .placeholder(R.drawable.avatar)
//                .into(holder.binding.profileImageItem);

        holder.binding.userNameItem.setText(userModel.getName());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                isSelectedMode = true;
                Log.d("click", "onLongClick: isSelectedMode" + isSelectedMode);

                if (selectedUserList.contains(userModel)) {
                    // contains userModel, so remove it
                    selectedUserList.remove(userModel);
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    // Doesn't contain userModel, so we add it
                    selectedUserList.add(userModel);
                    int color = ContextCompat.getColor(context, R.color.selectedRecyclerView);
                    holder.itemView.setBackgroundColor(color);

                    Log.d("select", "onClick: selected inside of LongClick(Adaptor)");

                }

                if (selectedUserList.size() == 0) {
                    isSelectedMode = false;


                }

                for (UserModel user : selectedUserList) {
                    Log.d("select", "List:  inside of LongClick(Adaptor) " + user.getName());
                }

                listener.onLongItemClick(holder.getAdapterPosition(),isSelectedMode);

                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("click", "onClick: isSelectedMode" + isSelectedMode);

                if (isSelectedMode) {

                    if (selectedUserList.contains(userModel)) {
                        selectedUserList.remove(userModel);
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT);

                    } else {
                        selectedUserList.add(userModel);
                        int color = ContextCompat.getColor(context, R.color.selectedRecyclerView);
                        holder.itemView.setBackgroundColor(color);

                    }
                    if (selectedUserList.size() == 0) {
                        isSelectedMode = false;
                        Log.d("select", "onClick: No Selection inside of Click(Adaptor)");
                    }
                    listener.onItemClick(holder.getAdapterPosition(),isSelectedMode);
                    for (UserModel user : selectedUserList) {
                        Log.d("select", "List: inside of Click(Adaptor) " + user.getName());
                    }
                } else {



                    if(userModel.isGroup() == true){
                        Intent intent = new Intent(context, GroupChatActivity.class);
                        intent.putExtra("name", userModel.getName());
                        intent.putExtra("groupId", userModel.getGroupId());
                        context.startActivity(intent);
                    }
                    else{
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("name", userModel.getName());
                        intent.putExtra("receiverId", userModel.getUserId());
                        intent.putExtra("profileImage", userModel.getProfileImage());
                        context.startActivity(intent);
                    }

                }
            }

            ;
        });

        if (!isSelectedMode) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }


    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConverstaionItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ConverstaionItemBinding.bind(itemView);


        }
    }
}
