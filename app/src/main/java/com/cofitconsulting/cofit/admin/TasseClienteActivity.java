package com.cofitconsulting.cofit.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.cofitconsulting.cofit.R;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



public class TasseClienteActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Spinner tipoF24, annoRif;
    private EditText importo, scadenza;
    private Button btnInserisci;
    private String userID;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasse_cliente);

        Intent intent = getIntent();
        userID = intent.getStringExtra("User_ID").trim();
        fStore = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tipoF24 = findViewById(R.id.spinnerF24);
        annoRif = findViewById(R.id.spinnerAnno);
        importo = findViewById(R.id.etImporto);
        scadenza = findViewById(R.id.etScadenza);
        btnInserisci = findViewById(R.id.btnInserisci);

        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 2017; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);
        annoRif.setAdapter(adapter);

        scadenza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int anno = calendar.get(Calendar.YEAR);
                int mese = calendar.get(Calendar.MONTH);
                int giorno = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(TasseClienteActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, anno, mese, giorno);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                String data = dayOfMonth + "/" + month + "/" + year;
                scadenza.setText(data);
            }
        };

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnInserisci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String f24 = tipoF24.getSelectedItem().toString().trim();
                String anno = annoRif.getSelectedItem().toString().trim();
                String valore = importo.getText().toString().trim() + "€";
                String dataScadenza = scadenza.getText().toString().trim();
                String totale = f24 + " " + anno;

                writeOnDatabaseTasse(f24, anno, valore, dataScadenza, totale);
                Toast.makeText(TasseClienteActivity.this, "Inserimento avvenuto", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    private void writeOnDatabaseTasse(String f24, String anno, String valore, String dataScadenza, String totale){
        Map<String, Object> user = new HashMap<>();
        user.put("Tassa", totale);
        user.put("Importo", valore);
        user.put("Scadenza", dataScadenza);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(userID).document(f24 + " " + anno).set(user);
    }
}