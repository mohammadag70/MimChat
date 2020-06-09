package mag.ir.mimchat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import mag.ir.mimchat.Activites.Chat.SingleChatActivity;
import mag.ir.mimchat.Models.Message;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

public class SingleChatListAdapter extends RecyclerView.Adapter<SingleChatListAdapter.MyViewHolder> {

    private List<Message> userMessageList;
    private Context context;

    private FirebaseAuth auth;
    private String currenUserId;
    private DatabaseReference userRefl;
    private LottieAnimationView loadingBar;

    public SingleChatListAdapter(List<Message> userMessageList, Context context, LottieAnimationView loadingBar) {
        this.userMessageList = userMessageList;
        this.context = context;
        this.loadingBar = loadingBar;

        if (userMessageList.size() == 0) {
            loadingBar.setVisibility(View.GONE);
        }

        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public SingleChatListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SingleChatListAdapter.MyViewHolder n = new SingleChatListAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false));
        return n;
    }

    @Override
    public void onBindViewHolder(@NonNull SingleChatListAdapter.MyViewHolder holder, int position) {
        currenUserId = auth.getCurrentUser().getUid();

        Message message = userMessageList.get(position);

        if (message.getType().equals("text")) {
            holder.chatContainer.setVisibility(View.VISIBLE);
            holder.imgContainer.setVisibility(View.GONE);
            holder.pwContainer.setVisibility(View.GONE);

            holder.date.setText(Utils.toPersianNumber(message.getDate()));
            holder.time.setText(Utils.toPersianNumber(message.getTime()));

            userRefl = FirebaseDatabase.getInstance().getReference().child("Users").child(message.getFrom());

            userRefl.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("image")) {
                        Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile).into(holder.image);
                    } else {
                        Picasso.get().load(R.drawable.profile).into(holder.image);
                    }

                    holder.name.setText(dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(message.getMessage());

            if (message.getFrom().equals(currenUserId)) {
                holder.root.setGravity(Gravity.RIGHT);
                holder.rel.setBackgroundColor(context.getResources().getColor(R.color.input));
            } else {
                holder.root.setGravity(Gravity.LEFT);
                holder.rel.setBackgroundColor(context.getResources().getColor(R.color.input2));
            }

            if (Utils.isPersian(message.getMessage())) {
                holder.message.setGravity(Gravity.RIGHT);
            } else {
                holder.message.setGravity(Gravity.LEFT);
            }

            holder.rel.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (message.getFrom().equals(currenUserId)) {
                        showDialog(position);
                    }
                    return false;
                }
            });


        } else if (message.getType().equals("image")) {
            holder.chatContainer.setVisibility(View.GONE);
            holder.imgContainer.setVisibility(View.VISIBLE);
            holder.pwContainer.setVisibility(View.GONE);

            holder.imgdate.setText(Utils.toPersianNumber(message.getDate()));
            holder.imgtime.setText(Utils.toPersianNumber(message.getTime()));
            Picasso.get().load(message.getMessage()).into(holder.photo);

            userRefl = FirebaseDatabase.getInstance().getReference().child("Users").child(message.getFrom());

            userRefl.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("image")) {
                        Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile).into(holder.imgimage);
                    }

                    holder.imgname.setText(dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            if (message.getFrom().equals(currenUserId)) {
                holder.root.setGravity(Gravity.RIGHT);
            } else {
                holder.root.setGravity(Gravity.LEFT);
            }

            holder.photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.popUpImage(context, message.getMessage());
                }
            });

            holder.imgrel.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (message.getFrom().equals(currenUserId)) {
                        showDialog(position);
                    }
                    return false;
                }
            });


        } else {
            holder.chatContainer.setVisibility(View.GONE);
            holder.imgContainer.setVisibility(View.GONE);
            holder.pwContainer.setVisibility(View.VISIBLE);

            holder.pwdate.setText(Utils.toPersianNumber(message.getDate()));
            holder.pwtime.setText(Utils.toPersianNumber(message.getTime()));

            userRefl = FirebaseDatabase.getInstance().getReference().child("Users").child(message.getFrom());

            userRefl.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("image")) {
                        Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile).into(holder.pwimage);
                    }

                    holder.pwname.setText(dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if (message.getType().equals("pdf")) {
                holder.pdfOrWordCVImage.setImageResource(R.drawable.ic_pdf);
            } else {
                holder.pdfOrWordCVImage.setImageResource(R.drawable.ic_word);
            }

            if (message.getFrom().equals(currenUserId)) {
                holder.root.setGravity(Gravity.RIGHT);
            } else {
                holder.root.setGravity(Gravity.LEFT);
            }

            holder.fileName.setText(message.getFileName());

            holder.pwrel.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (message.getFrom().equals(currenUserId)) {
                        showDialog(position);
                    }
                    return false;
                }
            });

            holder.pwrel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (message.getType().equals("pdf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(userMessageList.get(position).getMessage());
                        intent.setDataAndType(uri, "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(intent);
                    } else {
                        try {
                            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                            m.invoke(null);
                        } catch (Exception ignored) {
                        }

                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        File file = new File(userMessageList.get(position).getMessage());
                        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        if (extension.equalsIgnoreCase("") || mimetype == null) {
                            // if there is no extension or there is no definite mimetype, still try to open the file
                            intent.setDataAndType(Uri.fromFile(file), "text/*");
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), mimetype);
                        }
                        context.startActivity(intent);
                    }
                }
            });
        }

    }

    private void showDialog(int position) {
        CharSequence options[] = new CharSequence[]{
                "پاک کردن", "لغو"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("انتخاب کن!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    deleteSentMessage(position);
                }
                if (i == 1) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, time, date, message, imgname, imgtime, imgdate, pwname, pwtime, pwdate, fileName;
        carbon.widget.LinearLayout rel, pwrel, imgrel;
        CircleImageView image, imgimage, pwimage;
        RelativeLayout root;
        ImageView photo, pdfOrWordCVImage;
        carbon.widget.LinearLayout imgContainer, chatContainer, pwContainer;
        CardView pdfOrWordCV, photocv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            rel = itemView.findViewById(R.id.rel);
            root = itemView.findViewById(R.id.root);

            photo = itemView.findViewById(R.id.photo);
            photocv = itemView.findViewById(R.id.photocv);
            imgContainer = itemView.findViewById(R.id.imageContainer);
            chatContainer = itemView.findViewById(R.id.chatContainer);
            imgdate = itemView.findViewById(R.id.imgdate);
            imgtime = itemView.findViewById(R.id.imgtime);
            imgname = itemView.findViewById(R.id.imgname);
            imgimage = itemView.findViewById(R.id.imgimage);
            imgrel = itemView.findViewById(R.id.imgrel);

            pdfOrWordCV = itemView.findViewById(R.id.pdfOrWordCV);
            pdfOrWordCVImage = itemView.findViewById(R.id.pdfOrWordCVImage);
            pwname = itemView.findViewById(R.id.pwname);
            pwimage = itemView.findViewById(R.id.pwimage);
            pwtime = itemView.findViewById(R.id.pwtime);
            pwdate = itemView.findViewById(R.id.pwdate);
            fileName = itemView.findViewById(R.id.fileName);
            pwContainer = itemView.findViewById(R.id.pwContainer);
            pwrel = itemView.findViewById(R.id.pwrel);
        }
    }

    private void deleteSentMessage(final int position) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utils.showSuccessMessage(((SingleChatActivity) context), "پیام حذف شد");

                    userMessageList.remove(position);
                    SingleChatActivity.updateList();

                } else {
                    Utils.showErrorMessage(((SingleChatActivity) context), "خطا در حذف پیام");
                }
            }
        });
    }

}
