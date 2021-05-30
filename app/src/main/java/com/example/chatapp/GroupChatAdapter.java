package com.example.chatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.databinding.ActivityGroupChatBinding;
import com.example.chatapp.databinding.ItemReceiveBinding;
import com.example.chatapp.databinding.ItemSentBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter {
    ActivityGroupChatBinding binding;
    Context context;
    ArrayList<Message> messages;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    boolean isImageFitToScreen;

    public GroupChatAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())) {
            return ITEM_SENT;

        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        int reaction[] = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reaction)
                .build();
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (holder.getClass() == MessageAdapter.SentViewHolder.class) {
                MessageAdapter.SentViewHolder viewHolder = (MessageAdapter.SentViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reaction[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                GroupChatAdapter.ReceiverViewHolder viewHolder = (GroupChatAdapter.ReceiverViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reaction[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            message.setFeeling(pos);
            FirebaseDatabase.getInstance()
                    .getReference()
                    .child("public")
//                   .push()
                    .child(message.getMessageId())
                    .setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });

        if (holder.getClass() == GroupChatAdapter.SentViewHolder.class) {
            GroupChatAdapter.SentViewHolder viewHolder = (GroupChatAdapter.SentViewHolder) holder;
            if(message.getMessage().equals("photo")) {
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    viewHolder.binding.image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    viewHolder.binding.image.setAdjustViewBounds(true);
                }else{
                    isImageFitToScreen=true;
                    viewHolder.binding.image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    viewHolder.binding.image.setScaleType(ImageView.ScaleType.FIT_XY);
                }
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }
            viewHolder.binding.message.setText(message.getMessage());
            if (message.getFeeling() >= 0) {
//                message.setFeeling(reaction[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reaction[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);

                    return false;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);


                    return false;
                }
            });

        } else {
            GroupChatAdapter.ReceiverViewHolder viewHolder = (GroupChatAdapter.ReceiverViewHolder) holder;
            if(message.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    viewHolder.binding.image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    viewHolder.binding.image.setAdjustViewBounds(true);
                }else{
                    isImageFitToScreen=true;
                    viewHolder.binding.image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    viewHolder.binding.image.setScaleType(ImageView.ScaleType.FIT_XY);
                }
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }
            viewHolder.binding.message.setText(message.getMessage());
            if (message.getFeeling() >= 0) {
//                message.setFeeling(reaction[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reaction[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
