package br.com.acessibilidade.map;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import br.com.acessibilidade.map.models.Local;
import br.com.acessibilidade.map.network.EndpointClient;
import br.com.acessibilidade.map.network.Response;
import br.com.acessibilidade.map.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;

public class NewLocalActivity extends AppCompatActivity implements PlaceSelectionListener,
        GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemSelectedListener {

    private GoogleApiClient mGoogleApiClient;
    private EditText localNome;
    private EditText localDescricao;
    private Local local = new Local();
    private Double latitude;
    private Double longitude;
    private LinearLayout linearLayout;
    private List<String> acessoTemp = new ArrayList<String>();
    private Button btnSend;

    private List<String> checkboxAcessos = Arrays.asList("Rampa de acesso e calçada rebaixada",
            "Banheiros Adaptados", "Atendimento preferencial", "Vagas de estacionamento para deficientes",
            "Pisos táteis", "Alerta sonoro na travessia de pedestre", "Possui Terminal Telefônico para Surdos (TTS)");

    private List<String> checkboxInacessos = Arrays.asList("Dificuldade de acesso na entrada para deficientes físicos",
            "Calçada danificada nas proximidades", "Ruas nas proximidades com trânsito muito intenso dificultando a travessia",
            "Não possui fila preferencial para atendimento");

    private List<String> checkboxInstituicoes = Arrays.asList("Apoio a deficientes Fisicos motores",
            "Apoio a deficientes visuais", "Apoio a deficientes auditivos");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_local);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        linearLayout = findViewById(R.id.checkbox_acessos);
        localNome = findViewById(R.id.local_name);
        localDescricao = findViewById(R.id.descicao);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(this);
        autocompleteFragment.setHint("Encontre o local aqui");

        Spinner spinner = (Spinner) findViewById(R.id.types_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                local.setDescricao(localDescricao.getText().toString());
                local.setNome(localNome.getText().toString());

                String[] acessos = new String[acessoTemp.size()];
                acessoTemp.toArray(acessos);

                local.setAcessos(acessos);



                EndpointClient endpointClient = ServiceGenerator
                        .createService(EndpointClient.class, null);

                HashMap<String, Object> data = new HashMap<>();
                data.put("nome", local.getNome());
                data.put("latitude", local.getLatitude());
                data.put("longitude", local.getLongitude());
                data.put("descricao", local.getDescricao());
                data.put("tipo", local.getTipo());
                data.put("acessos", local.getAcessos2());

                Log.d("Successo", "Place Selected: " + local.toString());

              Call<Response<Local>> call = endpointClient.criarLocal(local.toString());
              call.enqueue(new Callback<Response<Local>>() {
                  @Override
                  public void onResponse(Call<Response<Local>> call, retrofit2.Response<Response<Local>> response) {
                      if(response.isSuccessful())
                         Log.d("", "isSuccessful" + response.body());
                      Log.d("", "falha " + response.code());
                  }

                  @Override
                  public void onFailure(Call<Response<Local>> call, Throwable t) {
                    t.printStackTrace();
                  }
              });
            }
        });

    }


    @Override
    public void onPlaceSelected(Place place) {
        localNome.setText(place.getName());
        local.setLatitude( place.getLatLng().latitude);
        local.setLongitude( place.getLatLng().longitude);
        //local.setNome( place.getName().toString());
    }

    @Override
    public void onError(Status status) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            parent.getItemAtPosition(pos);

            if(parent.getItemIdAtPosition(pos) == 0) {
                local.setTipo(1);
                populateCheckbox(checkboxAcessos);
            }
            else if (parent.getItemIdAtPosition(pos) == 1) {
                local.setTipo(2);
                populateCheckbox(checkboxInacessos);
            }
            else if(parent.getItemIdAtPosition(pos) == 2) {
                local.setTipo(3);
                populateCheckbox(checkboxInstituicoes);
            }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void populateCheckbox (List<String> list) {

        linearLayout.removeAllViews();
        acessoTemp.clear();

        for ( final String element : list) {
            final CheckBox checkBox = new CheckBox(getApplicationContext());
            checkBox.setText(element);
            checkBox.animate();
            checkBox.setTag(element);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        acessoTemp.add(checkBox.getText().toString());
                    } else {
                        acessoTemp.remove(checkBox.getText().toString());
                    }
                }
            });

            linearLayout.addView(checkBox);
        }
    }
}
