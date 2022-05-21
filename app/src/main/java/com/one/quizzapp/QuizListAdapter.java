package com.one.quizzapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Observer;

public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.QuizViewHolder> {

    private List<QuizListModel> quizListModel;
    private OnQuizListItemClicked onQuizListItemClicked;

    public QuizListAdapter(OnQuizListItemClicked onQuizListItemClicked) {
        this.onQuizListItemClicked = onQuizListItemClicked;
    }

    public void setQuizListModel(List<QuizListModel> quizListModel) {
        this.quizListModel = quizListModel;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        holder.listTitle.setText(quizListModel.get(position).getName());

        String imageUrl = quizListModel.get(position).getImage();

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .into(holder.listImage);

        String listDescription = quizListModel.get(position).getDesc();
        if(listDescription.length()>150){
            listDescription = listDescription.substring(0,150);
            listDescription = listDescription + "...";
        }
        holder.listDesc.setText(listDescription);
        holder.listLevel.setText(quizListModel.get(position).getLevel());

    }

    @Override
    public int getItemCount() {
        if(quizListModel == null){
            return 0;
        }else{
            return quizListModel.size();
        }
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView listImage;
        private TextView listTitle;
        private TextView listDesc;
        private TextView listLevel;
        private Button listBtn;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            listImage = itemView.findViewById(R.id.list_image);
            listTitle = itemView.findViewById(R.id.list_title);
            listDesc = itemView.findViewById(R.id.list_desc);
            listLevel = itemView.findViewById(R.id.list_difficulty);
            listBtn = itemView.findViewById(R.id.list_button);

            listBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onQuizListItemClicked.onItemClicked(getAdapterPosition());
        }
    }

    public interface OnQuizListItemClicked{
        void onItemClicked(int position);
    }
}
