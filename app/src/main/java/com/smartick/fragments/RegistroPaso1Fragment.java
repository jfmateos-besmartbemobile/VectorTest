package com.smartick.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gorgue.myapplication.R;


/**
 * Fragmento paso 1 del registro
 * Created by gorgue on 12/02/2015.
 */
public class RegistroPaso1Fragment extends Fragment {

    private ImageView iconoUsername;
    private ImageView iconoPassword;
    private TextView textUsername;
    private TextView textPassword;

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

        // Ponemos el foco en el username inicialmente
        textUsername.requestFocus();

        return rootView;
    }
}
