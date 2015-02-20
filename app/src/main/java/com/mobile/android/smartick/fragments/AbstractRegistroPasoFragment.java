package com.mobile.android.smartick.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;


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
