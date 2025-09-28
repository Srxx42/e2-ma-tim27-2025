package com.example.e2taskly.presentation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e2taskly.R;
import com.example.e2taskly.model.Message;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<Message> messages;
    private final String currentUserId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessages(List<Message> newMessages) {
        int startPosition = messages.size();
        messages.addAll(newMessages);
        notifyItemRangeInserted(startPosition, newMessages.size());
    }

    public void setMessages(List<Message> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }
    private class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessageText, textViewTimestamp;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            textViewMessageText = itemView.findViewById(R.id.textViewMessageText);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }

        void bind(Message message) {
            textViewMessageText.setText(message.getText());
            if (message.getTimestamp() != null) {
                textViewTimestamp.setText(dateFormat.format(message.getTimestamp()));
            }
        }
    }

    private class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewSenderName, textViewMessageText, textViewTimestamp;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            textViewSenderName = itemView.findViewById(R.id.textViewSenderName);
            textViewMessageText = itemView.findViewById(R.id.textViewMessageText);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }

        void bind(Message message) {
            textViewSenderName.setText(message.getSenderUsername());
            textViewMessageText.setText(message.getText());
            if (message.getTimestamp() != null) {
                textViewTimestamp.setText(dateFormat.format(message.getTimestamp()));
            }
        }
    }
}
