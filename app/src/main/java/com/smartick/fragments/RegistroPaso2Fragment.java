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
 * Fragmento paso 2 del registro
 * Created by gorgue on 12/02/2015.
 */
public class RegistroPaso2Fragment extends Fragment {

    private TextView textNombre;
    private TextView textApellidos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_registro_paso2, container, false);

        textNombre = (TextView)rootView.findViewById(R.id.registro_nombre);
        textApellidos = (TextView)rootView.findViewById(R.id.registro_apellidos);

        // Ponemos el foco en el username inicialmente
        textNombre.requestFocus();

        return rootView;
    }
}
