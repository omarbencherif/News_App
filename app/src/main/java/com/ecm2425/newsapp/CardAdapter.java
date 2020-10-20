
package com.ecm2425.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ImageViewHolder> {

    private int mySize;
    private ArrayList<Article> articleList;

    public interface ListItemClickListener {
    }


    public CardAdapter(ArrayList<Article> article, ListItemClickListener listener) {
        this.articleList = article;
        this.mySize = articleList.size();
    }

    @Override

    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForCardLayout = R.layout.cardlayout;
        LayoutInflater inflater = LayoutInflater.from(context);
        //inflates the xml layout
        View view = inflater.inflate(layoutIdForCardLayout, viewGroup, false);
        //Assigns the viewholder to the view
        ImageViewHolder viewHolder = new ImageViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onViewRecycled(ImageViewHolder holder) {
    }

    @Override
    //binds the data to the textView
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        //Gets the article at the current position
        Article thisArticle = articleList.get(holder.getAdapterPosition());
        //Sets the text fields and the photo
        holder.viewHolderText.setText(thisArticle.getTitle());
        holder.viewHolderSource.setText(thisArticle.getNewsOutlet());
        holder.URL = thisArticle.getURL();
        //Picasso is called to download, cache and set the image
        Picasso.get().load(thisArticle.getPictureURL()).into(holder.viewHolderImage);
        //The viewholder has a clicklistener so that it opens a link when it is clicked
        holder.viewHolderImage.setOnClickListener(holder.myClickListener);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mySize;
    }


    class ImageViewHolder extends RecyclerView.ViewHolder {
        // Will display the position in the list
        ImageView viewHolderImage;
        // Will display which ViewHolder is displaying this data
        TextView viewHolderText;
        TextView viewHolderSource;
        String URL;


        public OnClickListener myClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                v.getContext().startActivity(browserIntent);

            }
        };
        //Constructor for  ImageViewHolder
        ImageViewHolder(View itemView) {
            super(itemView);
            viewHolderText = itemView.findViewById(R.id.tv_view_holder_instance);
            viewHolderImage = itemView.findViewById(R.id.iv_view_holder_instance);
            viewHolderSource = itemView.findViewById(R.id.dv_view_holder_instance);


        }


    }

}

