package com.softappsuganda.cheapinternationalsmsapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.softappsuganda.cheapinternationalsmsapp.R;
import com.softappsuganda.cheapinternationalsmsapp.helpers.Tools;
import com.softappsuganda.cheapinternationalsmsapp.models.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AdapterMessageList extends FirestoreRecyclerAdapter<Message,RecyclerView.ViewHolder> {

    private List<Message> items = new ArrayList<>();
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;


    // Replace the contents of a view (invoked by the layout_no_item manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message model) {
            if (holder instanceof OriginalViewHolder) {
                final OriginalViewHolder rootView = (OriginalViewHolder) holder;
                final Message message = model;

                //set title of action
                rootView.title.setText(message.getPhone());
                ImageView starImageView = rootView.imageViewStar;
                View paretnStarImageView = rootView.lytImageViewStar;

                String status = message.getStatus();
                rootView.section.setText(status);
                String elipsis = message.getSms_text().length()>20?"...":"";
                rootView.code.setText(message.getSms_text().substring(0,Math.min(20,message.getSms_text().length()))+elipsis);

                if(!message.getStatus().equalsIgnoreCase("Delivered")){
                    starImageView.setVisibility(View.VISIBLE);
                }

                String icon_letter = String.valueOf((message.getSms_text()).trim().charAt(0)).toUpperCase();
                if (!icon_letter.matches("[a-z]")) {//if the first character is not alphabetic
                    //find the first alphabetic character in the name of this code
                    Pattern pattern = Pattern.compile("\\p{Alpha}");
                    Matcher matcher = pattern.matcher(message.getSms_text());
                    if (matcher.find()) {
                        icon_letter = matcher.group();
                    }
                }
                TextDrawable drawable = TextDrawable.builder()
                        .buildRound(icon_letter, ctx.getResources().getColor(R.color.colorPrimary));
                if (null != rootView.image) {
                    if (drawable != null) {
                        try {
                            rootView.image.setImageDrawable(drawable);
                        } catch (Exception e) {
                            //Toast.makeText(get, "Some features may not work", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (true) {
                    starImageView.setColorFilter(ContextCompat.getColor(ctx, R.color.green_300), android.graphics.PorterDuff.Mode.SRC_IN);
                }
//                rootView.linearLayout.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String id = getSnapshots().getSnapshot(position).getId();
//                        Log.d("TOKEN",id);
//                        if (mOnItemClickListener != null) {
////                            mOnItemClickListener.onItemClick(view, ussdActionWithStepsFiltered.get(position), position);
//                        }
//                    }
//                });
//                rootView.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        if (mOnItemClickListener != null) {
////                            mOnItemClickListener.onLongClick(v, ussdActionWithStepsFiltered.get(position), position);
//                        }
//                        return true;
//                    }
//                });
                starImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = getSnapshots().getSnapshot(position).getId();
                        Toast.makeText(ctx,"Sending...",Toast.LENGTH_LONG).show();
                        Tools.sendSms(ctx,message.getPhone(),message.getSms_text(),id);
                    }
                });
//                paretnStarImageView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String id = getSnapshots().getSnapshot(position).getId();
//                        Log.d("TOKEN star",id);
////                        Tools.sendSms(ctx,message.getPhone(),message.getSms_text(),id);
//                    }
//                });
        }
    }


    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterMessageList(@NonNull FirestoreRecyclerOptions<Message> options, Context context) {
        super(options);
        this.ctx = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Message obj, int position);

        void onLongClick(View v,  Message message, int position);

        void onStarClick(View v, Message message, int position);

    }



    public static class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image, imageViewStar;
        public TextView title, code, section;
        public LinearLayout linearLayout, lytImageViewStar;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.ImageView_ActionIcon);
            imageViewStar = v.findViewById(R.id.imageViewStar);
            lytImageViewStar = v.findViewById(R.id.lyt_imageViewStar);
            title = (TextView) v.findViewById(R.id.TextView_ActionName);
            code = (TextView) v.findViewById(R.id.TextView_ActionCode);
            section = (TextView) v.findViewById(R.id.TextView_Action_section);
            linearLayout = v.findViewById(R.id.myAction);
        }
    }

    /*
 adapted from mkyong.com
 */
    public static boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().trim().replaceAll(" ", "").contains(subString.toLowerCase().trim().replaceAll(" ", ""));
    }
}
