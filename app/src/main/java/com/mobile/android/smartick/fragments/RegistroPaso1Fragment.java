package com.mobile.android.smartick.fragments;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.pojos.UserStatus;
import com.mobile.android.smartick.util.Constants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;


/**
 * Fragmento paso 1 del registro
 * Created by gorgue on 12/02/2015.
 */
public class RegistroPaso1Fragment extends AbstractRegistroPasoFragment {

    private ImageView iconoUsername;
    private ImageView iconoPassword;
    private TextView textUsername;
    private TextView textPassword;

    // Numero de paso
    private final static int numOrden = 0;

    @Override
    public int getNumOrdern() {
        return numOrden;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_registro_paso1, container, false);

        textUsername = (TextView)rootView.findViewById(R.id.registro_alias);
        textPassword = (TextView)rootView.findViewById(R.id.registro_password);
        iconoUsername = (ImageView)rootView.findViewById(R.id.registro_student_username);
        iconoPassword = (ImageView)rootView.findViewById(R.id.registro_student_password);
        // Segun el foco del campo de texto cambiamos la imagen junto al textview
        textUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    iconoUsername.setSelected(true);
                }else {
                    iconoUsername.setSelected(false);
                }
            }
        });
        textPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    iconoPassword.setSelected(true);
                }else {
                    iconoPassword.setSelected(false);
                }
            }
        });

        // Click al siguiente
        Button next = (Button)rootView.findViewById(R.id.registro_paso1_forward_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Comprobacion de si es un alias valido
                new LoginMobile().execute(textUsername.getText().toString(), textPassword.getText().toString());
            }
        });

        // Ponemos el foco en el username inicialmente
        textUsername.requestFocus();

        return rootView;
    }

    /**
     * Dialogo de error
     */
    private void showAlertDialog(int msg){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(getString(msg));
        // TODO
        //alertDialog.setIcon(android.R.drawable.ic_delete);
        alertDialog.show();
    }

    private class LoginMobile extends AsyncTask<String, Void, UserStatus> {

        @Override
        protected UserStatus doInBackground(String... params) {
            try {
                // Recuperamos los datos del usuario
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                UserStatus user = restTemplate.getForObject(Constants.LOGIN_MOBILE_SERVICE, UserStatus.class, params[0], params[1]);
                return user;
            } catch (Exception e) {
                Log.e("LoginMobile", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(UserStatus user) {

            // Comprobamos que el username no esta en uso
            if (user == null) {
                showAlertDialog(R.string.You_must_be_connected_to_the_internet);
            } else if (!user.getStatus().equals(UserStatus.Status.login_invalid)){
                showAlertDialog(R.string.username_not_valid_or_already_exists);
            } else {
                // Los datos son validos
                registroPager.setCurrentItem(numOrden + 1);
            }
        }

    }

}
