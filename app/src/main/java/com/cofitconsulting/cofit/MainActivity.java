package com.cofitconsulting.cofit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cofitconsulting.cofit.admin.ListaClientiActivity;
import com.cofitconsulting.cofit.user.anagrafica.InserimentoAnagraficaActivity;
import com.cofitconsulting.cofit.user.anagrafica.ModificaAnagraficaActivity;
import com.cofitconsulting.cofit.user.documenti.CaricaDocUsersActivity;
import com.cofitconsulting.cofit.user.documenti.VisualizzaDocUsersActivity;
import com.cofitconsulting.cofit.user.documenti.VisualizzaNovitaActivity;
import com.cofitconsulting.cofit.utility.adaptereviewholder.PageAdapterMainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabItem crediti, debiti, tasse;
    private PagerAdapter adapter;
    private String email;
    private FirebaseAuth fAuth;
    private StorageReference storageReference;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } catch(NullPointerException e)
        {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        }

        //se l'utente ha uno di questi indirizzi entra nella versione admin
        if (email.equals("francesco0792@gmail.com") || email.equals("cofitconsulting@outlook.it")) {
            Intent intent = new Intent(MainActivity.this, ListaClientiActivity.class);
            startActivity(intent);
            finish();
        } else {

            //utilizzo le sharedPreferences per far inserire l'anagrafica al primo avvio
            SharedPreferences preferences = getSharedPreferences("anagrafica", MODE_PRIVATE);  //se il boolean firstrun all'interno delle Preferences è true lanciamo la LoginActivity con l'intent.
            if (preferences.getBoolean("firstrun", true)) {
                Toast.makeText(MainActivity.this, "INSERISCI SUBITO LA TUA ANAGRAFICA", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("firstrun", false);
                editor.apply();
                Intent intent = new Intent(this, InserimentoAnagraficaActivity.class);
                startActivity(intent);
            }

        }

        toolbar.setTitle("Benvenuto in Cofit");
        tabLayout = findViewById(R.id.tableLayout);
        viewPager = findViewById(R.id.view_pager);
        crediti = findViewById(R.id.crediti);
        debiti = findViewById(R.id.debiti);
        tasse = findViewById(R.id.tasse);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        View headerView = navigationView.getHeaderView(0);
        TextView text_email = headerView.findViewById(R.id.email);
        text_email.setText(email);

        final CircleImageView profileImage = headerView.findViewById(R.id.profileImage);
        storageReference = FirebaseStorage.getInstance().getReference();
        String userID = fAuth.getInstance().getCurrentUser().getUid();
        final StorageReference profileRef = storageReference.child("users/" + userID + "profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });

        adapter = new PageAdapterMainActivity(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager, true);
        tabLayout.getTabAt(0).setText("F24/Tasse");
        tabLayout.getTabAt(1).setText("Crediti");
        tabLayout.getTabAt(2).setText("Debiti");


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.menuProfile: {
                Intent intent = new Intent(MainActivity.this, ModificaAnagraficaActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.menuNovita: {
                Intent intent = new Intent(MainActivity.this, VisualizzaNovitaActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.menuInserisciDoc: {
                Intent intent = new Intent(MainActivity.this, CaricaDocUsersActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menuVisualizzaDoc: {
                Intent intent = new Intent(MainActivity.this, VisualizzaDocUsersActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menuIndirizzo: {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=via+Gabriele+d'annunzio+24+pescara"));
                startActivity(intent);
                break;
            }
            case R.id.menuTelefono: {
                String phone = "085377395";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
                break;
            }
            case R.id.menuWhatsapp: {
                String mobileNumber = "3401861219";
                boolean installed = appInstalledOnNot("com.whatsapp");

                if (installed) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + "39" + mobileNumber));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "WhatsApp non è installato sul tuo dispositivo", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.menuFacebook: {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + "219948828196163"));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent =  new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + "219948828196163"));
                    startActivity(intent);
                }
                break;
            }
            case R.id.menuEmail:
            {
                String ind_email = "info@cofit.consulting.com";
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{ind_email});
                try {
                    startActivity(Intent.createChooser(i, "Invia email..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "Client di posta non disponibile", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.menuWeb: {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.cofitconsulting.com")));
                break;
            }


            case R.id.menuEntrate: {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.agenziaentrate.gov.it/portale/home")));
                break;
                }

            case R.id.menuRiscossione: {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.agenziaentrateriscossione.gov.it/it/")));
                break;
            }
            case R.id.menuInps: {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.inps.it/nuovoportaleinps/default.aspx")));
                break;
            }

            case R.id.menuInail: {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.inail.it/cs/internet/home.html")));
                break;
            }

            case R.id.menuCciaa: {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.pe.camcom.it/")));
                break;
            }

            case R.id.menuEsci: {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            }
            default: return false;
        }
        return false;
    }


    private boolean appInstalledOnNot(String url) {
        PackageManager packageManager = getPackageManager();
        boolean app_installed;
        try{
            packageManager.getPackageInfo(url, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed;
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }


}
