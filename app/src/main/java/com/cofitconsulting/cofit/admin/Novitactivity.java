package com.cofitconsulting.cofit.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.cofitconsulting.cofit.R;
import com.cofitconsulting.cofit.utility.strutture.StrutturaUpload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Novitactivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnScegliImag, btnCaricaImag;
    private EditText fileName;
    private Spinner spinnerNomeDoc;
    private ImageView imageView, btnBack;
    private ProgressBar progressBar;

    private Uri fileUri;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 107;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carica_documento);

        btnScegliImag = findViewById(R.id.btnScegliFile);
        btnCaricaImag = findViewById(R.id.btnCarica);
        fileName = findViewById(R.id.nomeFile);
        spinnerNomeDoc = findViewById(R.id.spinner_nomeFile);
        spinnerNomeDoc.setVisibility(View.INVISIBLE);
        imageView = findViewById(R.id.imageView);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar3);
        storageReference = FirebaseStorage.getInstance().getReference("Novità");
        databaseReference = FirebaseDatabase.getInstance().getReference("Novità");

        btnScegliImag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissions.add(Manifest.permission.CAMERA);
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissionsToRequest = findUnaskedPermissions(permissions);
                if(permissionsToRequest.size()>0)//se abbiamo qualche permesso da richiedere
                {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),ALL_PERMISSIONS_RESULT);
                }
                else {
                    openFileChooser();
                }
            }
        });

        btnCaricaImag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = fileName.getText().toString().trim();
                Toast.makeText(Novitactivity.this, "Caricamento in corso", Toast.LENGTH_SHORT).show();
                if(TextUtils.isEmpty(nome))
                {
                    fileName.setError("Inserire il nome del file");
                    return;
                } else
                {
                    uploadFile();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            fileUri = data.getData();
            Picasso.get().load(fileUri).into(imageView);
        }
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if(fileUri != null)
        {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(fileUri));

            uploadTask = fileReference.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(Novitactivity.this, "File caricato", Toast.LENGTH_SHORT).show();
                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while(! urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();
                            String nomeCompleto = fileName.getText().toString().trim();
                            StrutturaUpload strutturaUpload = new StrutturaUpload(nomeCompleto, downloadUrl.toString());

                            String uploadId = databaseReference.push().getKey();
                            databaseReference.child(uploadId).setValue(strutturaUpload);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Novitactivity.this, "Caricamento fallito", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        } else
        {
            Toast.makeText(this, "Nessun file selezionato", Toast.LENGTH_SHORT).show();
        }

    }

    //Metodo per cercare i permessi non dati
    private ArrayList findUnaskedPermissions(ArrayList<String> wanted){
        ArrayList<String> result = new ArrayList<>();
        for(String perm : wanted) //per ogni permesso cercato
        {
            //se il permesso NON è stato dato allora lo dobbiamo richiedere
            if(!(Novitactivity.this.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED))
            {
                result.add(perm);
            }
        }
        return result;
    }

    //Metodo per sapere se i permessi sono stati dati
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult){
        if(requestCode == ALL_PERMISSIONS_RESULT)
        {
            for(String perm : permissionsToRequest)//per ogni permesso in permissionsToRequest
            {
                if(!(Novitactivity.this.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED)) //se non è stato dato
                {
                    permissionsRejected.add(perm);//lo aggiungiamo in permissionRejected
                }
            }
            if(permissionsRejected.size()>0) //se c'è almeno uno rifiutato diciamogli che dovrebbe accettarli con un TOAST
            {
                if(shouldShowRequestPermissionRationale(permissionsRejected.get(0)))
                {
                    Toast.makeText(Novitactivity.this, "Approva tutto", Toast.LENGTH_SHORT).show();
                }
            }
            else //altrimenti puoi procedere per cambiare l'immagine.
            {
                openFileChooser();
            }
        }
    }

}
