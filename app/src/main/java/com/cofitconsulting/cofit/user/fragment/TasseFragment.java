package com.cofitconsulting.cofit.user.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cofitconsulting.cofit.R;
import com.cofitconsulting.cofit.utility.StrutturaTassa;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TasseFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private RecyclerView mRecyclerView;
    private FirestoreRecyclerAdapter adapter;
    private FirebaseAuth fAuth;
    private String userID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tasse, container, false);

        userID = fAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();


        mRecyclerView = v.findViewById(R.id.recyclerview_tasse);

        Query query = firebaseFirestore.collection(userID).orderBy("Tassa");
        final FirestoreRecyclerOptions<StrutturaTassa> options = new FirestoreRecyclerOptions.Builder<StrutturaTassa>()
                .setQuery(query, StrutturaTassa.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<StrutturaTassa, UserViewHolder>(options) {
            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lv_item_tasse_cliente, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull StrutturaTassa model) {
                holder.text_tassa.setText(model.getTassa());
                holder.text_scadenza.setText(model.getScadenza());
                holder.text_importo.setText(model.getImporto());
                holder.text_pagato.setText(model.getPagato());
                String dataScadenza = model.getScadenza();
                String pagato = holder.text_pagato.getText().toString();
                if(scaduto(dataScadenza) && pagato.equals("No"))
                {
                    holder.text_scadenza.setText(model.getScadenza() + " " +"SCADUTO");
                    holder.text_scadenza.setTextColor(Color.RED);
                }

            }
        };

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        return v;
    }

    private class UserViewHolder extends RecyclerView.ViewHolder{

        private TextView text_tassa;
        private TextView text_importo;
        private TextView text_scadenza;
        private TextView text_pagato;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            text_tassa = itemView.findViewById(R.id.txt_tassa);
            text_importo = itemView.findViewById(R.id.txt_importo);
            text_scadenza = itemView.findViewById(R.id.txt_scadenza);
            text_pagato = itemView.findViewById(R.id.txt_pagato);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    public boolean scaduto(String data_scadenza){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/M/yyyy");
        Date currentTime = Calendar.getInstance().getTime();
        try {
            Date date = dateFormat.parse(data_scadenza);
            if(currentTime.after(date))
            {
               return true;
            }
            else return false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

}
