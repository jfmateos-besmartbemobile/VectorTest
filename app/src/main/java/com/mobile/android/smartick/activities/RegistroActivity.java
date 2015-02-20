package com.mobile.android.smartick.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.fragments.RegistroPaso1Fragment;
import com.mobile.android.smartick.fragments.RegistroPaso2Fragment;
import com.mobile.android.smartick.fragments.RegistroPaso3Fragment;

/**
 * Activity para registro de alumnos y tutores
 */
public class RegistroActivity extends FragmentActivity {

    // Pager adapter
    FragmentPagerAdapter registroViewPager;

    // Pager widget
    ViewPager registroPager;

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Instanciamos ViewPager y PagerAdapter
        registroPager = (ViewPager)findViewById(R.id.registro_pager);
        registroViewPager = new RegistroPagerAdapter(getSupportFragmentManager());
        registroPager.setAdapter(registroViewPager);

        // Arrancamos con el paso 1 del registro
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        RegistroPaso1Fragment paso1 = new RegistroPaso1Fragment();
//        fragmentTransaction.replace(R.id.paso_registro, paso1);
//        fragmentTransaction.commit();

    }


    /**
     * Siguiente paso en el registro
     * @param view
     */
    public void irSiguiente(View view) {
        // Recuperamos el usuario y el password
        //username = textUsername.getText().toString();
        //password = textPassword.getText().toString();

        // Comprobar usuario y pass
        // TODO

        // Vamos al siguiente paso
        // TODO
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class RegistroPagerAdapter extends FragmentPagerAdapter {

        // Numero de pasos
        private static final int NUM_PAGES = 3;

        public RegistroPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                // Paso 0
                case 0:
                    RegistroPaso1Fragment paso1 = new RegistroPaso1Fragment();
                    paso1.setRegistroPager(registroPager);
                    return paso1;
                case 1:
                    RegistroPaso2Fragment paso2 = new RegistroPaso2Fragment();
                    paso2.setRegistroPager(registroPager);
                    return paso2;
                case 2:
                    RegistroPaso3Fragment paso3 = new RegistroPaso3Fragment();
                    paso3.setRegistroPager(registroPager);
                    return paso3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
