package com.mobile.android.smartick.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobile.android.smartick.R;


/**
 * Fragmento paso 3 del registro
 * Created by gorgue on 12/02/2015.
 */
public class RegistroPaso3Fragment extends AbstractRegistroPasoFragment {

    // Numero de paso
    private final static int numOrden = 2;

    @Override
    public int getNumOrdern() {
        return numOrden;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_registro_paso3, container, false);

        // Click al siguiente
        Button next = (Button)rootView.findViewById(R.id.registro_paso3_forward_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registroPager.setCurrentItem(numOrden + 1);
            }
        });

        // Click al anterior
        Button back = (Button)rootView.findViewById(R.id.registro_paso3_back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registroPager.setCurrentItem(numOrden - 1);
            }
        });

        return rootView;
    }

    /**
     * Seleccion chico
     * @param v vista
     */
    public void seleccionChico(View v) {
        // TODO

        // Paso siguiente
        registroPager.setCurrentItem(numOrden + 1);
    }

    /**
     * Seleccion chica
     * @param v vista
     */
    public void seleccionChica(View v) {
        // TODO

        // Paso siguiente
        registroPager.setCurrentItem(numOrden + 1);
    }

}
