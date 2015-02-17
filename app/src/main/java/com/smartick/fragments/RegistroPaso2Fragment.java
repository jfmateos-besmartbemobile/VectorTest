package com.smartick.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gorgue.myapplication.R;


/**
 * Fragmento paso 2 del registro
 * Created by gorgue on 12/02/2015.
 */
public class RegistroPaso2Fragment extends AbstractRegistroPasoFragment {

    private TextView textNombre;
    private TextView textApellidos;

    // Numero de paso
    private final static int numOrden = 1;

    @Override
    public int getNumOrdern() {
        return numOrden;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_registro_paso2, container, false);

        textNombre = (TextView)rootView.findViewById(R.id.registro_nombre);
        textApellidos = (TextView)rootView.findViewById(R.id.registro_apellidos);

        // Click al siguiente
        Button next = (Button)rootView.findViewById(R.id.registro_paso2_forward_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registroPager.setCurrentItem(numOrden + 1);
            }
        });

        // Click al anterior
        Button back = (Button)rootView.findViewById(R.id.registro_paso2_back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registroPager.setCurrentItem(numOrden - 1);
            }
        });

        // Ponemos el foco en el username inicialmente
        textNombre.requestFocus();

        return rootView;
    }
}
