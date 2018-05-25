package com.example.sanay.blog;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.myViewHolder> {

    public List<BlogPost> blogPosts;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter(List<BlogPost> blogPosts) {
        this.blogPosts = blogPosts;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = firebaseAuth.getInstance();
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlogRecyclerAdapter.myViewHolder holder, int position) {

        final String blogPostId = blogPosts.get(position).BlogPostId;
        final String currentUserId =  firebaseAuth.getCurrentUser().getUid();
        holder.setIsRecyclable(false);

        String descData = blogPosts.get(position).getDescription();

        String imageUrl = blogPosts.get(position).getImageurl();
        String thumbUrl = blogPosts.get(position).getThumbnailurl();
        String userId = blogPosts.get(position).getUserid();
        holder.set(descData, imageUrl, thumbUrl);

        //user data
        firebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String username = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(username, userImage);

                } else {

                }
            }
        });

        try {
            long millisecond = blogPosts.get(position).getTimestamp().getTime();
            String dateString = TimeAgo.using(millisecond);
            holder.setDate(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        //Get Likes Count
        firebaseFirestore.collection("posts/" + blogPostId + "/likes").addSnapshotListener((Activity)context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()){

                    int count = documentSnapshots.size();

                    holder.updateLikesCount(count);

                } else {

                    holder.updateLikesCount(0);

                }

            }
        });

        //Get Likes
        firebaseFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).addSnapshotListener((Activity)context,new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists()){

                    holder.likeButton.setImageDrawable(context.getDrawable(R.mipmap.ic_like_liked));

                } else {

                    holder.likeButton.setImageDrawable(context.getDrawable(R.mipmap.ic_like));

                }

            }
        });

        //like button code
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).get().addOnCompleteListener((Activity)context,new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists())
                        {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).set(likesMap);

                        }
                        else
                        {
                            firebaseFirestore.collection("posts/" + blogPostId + "/likes").document(currentUserId).delete();
                        }
                    }
                });

            }


        });
    }

    @Override
    public int getItemCount() {
        return blogPosts.size();
    }


    public class myViewHolder extends RecyclerView.ViewHolder {

        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private ImageView likeButton;
        private TextView blogLikeCount;

        View mView;

        public myViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            likeButton = mView.findViewById(R.id.blog_like_btn);
        }

        public void set(String text, String imageUrl, String thumbUrl) {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(text);
            blogImageView = mView.findViewById(R.id.blog_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.defaultscenery);

            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(imageUrl)
                    .thumbnail(Glide.with(context).load(thumbUrl))
                    .into(blogImageView);


        }

        public void setDate(String Date) {
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(Date);
        }

        public void setUserData(String name, String image) {
            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName = mView.findViewById(R.id.blog_user_name);

            blogUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.mipmap.ic_account_circle);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);
        }

        public void updateLikesCount(int count){

            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count+" ");

        }
    }


}
