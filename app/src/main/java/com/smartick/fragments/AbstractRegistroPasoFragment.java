package com.smartick.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gorgue.myapplication.R;


/**
 * Fragmento paso 1 del registro
 * Created by gorgue on 12/02/2015.
 */
public abstract class AbstractRegistroPasoFragment extends Fragment {

    // Referencia al paginador
    protected ViewPager registroPager;

    public void setRegistroPager(ViewPager registroPager) {
        this.registroPager = registroPager;
    }

    /**
     * Numero del paso dentro del flujo
     * @return numero de paso
     */
    public abstract int getNumOrdern();

}
