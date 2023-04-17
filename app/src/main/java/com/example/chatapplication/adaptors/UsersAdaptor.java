package com.example.chatapplication.adaptors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.activities.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ConverstaionItemBinding;
import com.example.chatapplication.models.UserModel;
import com.example.chatapplication.utils.Credentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class UsersAdaptor extends RecyclerView.Adapter<UsersAdaptor.ViewHolder> {

    private Context context;
    private List<UserModel> userModelList;

    public UsersAdaptor(Context context, List<UserModel> userModelList) {
        this.context = context;
        this.userModelList = userModelList;
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

        FirebaseDatabase.getInstance()
                .getReference()
                .child(Credentials.DATABASE_REF_CHATS)
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                            String lastMsg = snapshot.child(Credentials.DATABASE_REF_LAST_MSG).getValue(String.class);
                            long lastMsgTime = snapshot.child(Credentials.DATABASE_REF_LAST_MSG_TIME).getValue(Long.class);

                            holder.binding.lastMessageItem.setText(lastMsg);

                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.timeItem.setText(dateFormat.format(new Date(lastMsgTime)));


                        }
                        else{
                            holder.binding.lastMessageItem.setText("Tap To Chat...");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        Picasso.get().load(userModel.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profileImageItem);

        holder.binding.userNameItem.setText(userModel.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name", userModel.getName());
                intent.putExtra("receiverId", userModel.getUserId());
                context.startActivity(intent);
            }
        });
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
