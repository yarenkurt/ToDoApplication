package com.example.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserId;

    private ProgressDialog loader;

    private String key="";
    private String task;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ToDo List App");
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.todoRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        loader = new ProgressDialog(this);

        mUser = mAuth.getCurrentUser();
        onlineUserId = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserId);

        fab = findViewById(R.id.homeFab);
        fab.setOnClickListener(v -> addTask());
    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file,null);
        myDialog.setView(myView);

        AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final EditText task = myView.findViewById(R.id.task_txt);
        final EditText description = myView.findViewById(R.id.description_txt);
        Button save = myView.findViewById(R.id.savebtn);
        Button cancel = myView.findViewById(R.id.cancelbtn);

        cancel.setOnClickListener(v -> dialog.dismiss());
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mTask = task.getText().toString().trim();
                String mDescription = description.getText().toString().trim();
                String id = reference.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                if(TextUtils.isEmpty(mTask)){
                    task.setError("Task Required!");
                    return;
                }
                if(TextUtils.isEmpty(mDescription)){
                    description.setError("Description Required!");
                }else{
                    loader.setMessage("Adding Your Data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    Model model = new Model(mTask,mDescription,id,date);
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(HomeActivity.this, "Task has been inserted successfullly", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }else{
                                String error = task.getException().toString();
                                Toast.makeText(HomeActivity.this, "Failed: "+ error, Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });

                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(reference,Model.class).build();
        FirebaseRecyclerAdapter<Model,MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Model model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDescription(model.getDescription());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        task = model.getTask();
                        description = model.getDescription();
                        
                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieved_layout,parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTask(String task){
            TextView taskTxt = mView.findViewById(R.id.taskTxt);
            taskTxt.setText(task);

        }

        public void setDescription(String description){
            TextView descriptionTxt = mView.findViewById(R.id.descriptionTxt);
            descriptionTxt.setText(description);
        }

        public void setDate(String date){
            TextView dateTxt = mView.findViewById(R.id.dateTxt);
            dateTxt.setText(date);
        }
    }

    private void updateTask() {
        AlertDialog.Builder updateDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data,null);
        updateDialog.setView(view);

        AlertDialog dialog = updateDialog.create();

        EditText updateTask = view.findViewById(R.id.updateTaskTxt);
        EditText updateDescription = view.findViewById(R.id.updateDescriptionTxt);

        updateTask.setText(task);
        updateTask.setSelection(task.length());

        updateDescription.setText(description);
        updateDescription.setSelection(description.length());

        Button updateBtn = view.findViewById(R.id.updatebtn);
        Button deleteBtn = view.findViewById(R.id.deletebtn);

        updateBtn.setOnClickListener(v -> {
            task = updateTask.getText().toString().trim();
            description = updateDescription.getText().toString().trim();

            String date = DateFormat.getDateInstance().format(new Date());

            Model model = new Model(task,description,key,date);

            reference.child(key).setValue(model).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(HomeActivity.this, "Data is updated successfully!", Toast.LENGTH_SHORT).show();
                }else{
                    String error = task.getException().toString();
                    Toast.makeText(HomeActivity.this, "Updating is failed "+error, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(key).removeValue().addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(HomeActivity.this, "Data is deleted successfully!", Toast.LENGTH_SHORT).show();
                    }else{
                        String error = task.getException().toString();
                        Toast.makeText(HomeActivity.this, "Deleting is failed "+error, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout :
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

        }
        return super.onOptionsItemSelected(item);
    }
}