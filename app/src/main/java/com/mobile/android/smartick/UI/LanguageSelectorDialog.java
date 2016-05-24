package com.mobile.android.smartick.UI;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mobile.android.smartick.R;
import com.mobile.android.smartick.util.LocaleHelper;

/**
 * Created by sbarrio on 23/5/16.
 */
public class LanguageSelectorDialog extends DialogFragment{

    LanguageSelectorInterface languageSelectorInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            languageSelectorInterface = (LanguageSelectorInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_language_selector_dialog, container, false);
        getDialog().setTitle(getString(R.string.set_language));

        Button dismiss = (Button) rootView.findViewById(R.id.lang_cancel_button);
        dismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button en_button = (Button) rootView.findViewById(R.id.lang_en_button);
        en_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("en");
            }
        });

        Button es_button = (Button) rootView.findViewById(R.id.lang_es_button);
        es_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es");
            }
        });

        Button es_AR_button = (Button) rootView.findViewById(R.id.lang_es_AR_button);
        es_AR_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-AR");
            }
        });

        Button es_BO_button = (Button) rootView.findViewById(R.id.lang_es_BO_button);
        es_BO_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-BO");
            }
        });

        Button es_CL_button = (Button) rootView.findViewById(R.id.lang_es_CL_button);
        es_CL_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-CL");
            }
        });

        Button es_CO_button = (Button) rootView.findViewById(R.id.lang_es_CO_button);
        es_CO_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-CO");
            }
        });

        Button es_EC_button = (Button) rootView.findViewById(R.id.lang_es_EC_button);
        es_EC_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-EC");
            }
        });

        Button es_MX_button = (Button) rootView.findViewById(R.id.lang_es_MX_button);
        es_MX_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-MX");
            }
        });

        Button es_PE_button = (Button) rootView.findViewById(R.id.lang_es_PE_button);
        es_PE_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-PE");
            }
        });


        Button es_PY_button = (Button) rootView.findViewById(R.id.lang_es_PY_button);
        es_PY_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-PY");
            }
        });


        Button es_UY_button = (Button) rootView.findViewById(R.id.lang_es_UY_button);
        es_UY_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-UY");
            }
        });

        Button es_VE_button = (Button) rootView.findViewById(R.id.lang_es_VE_button);
        es_VE_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setLanguage("es-VE");
            }
        });

        return rootView;
    }
    private void setLanguage(String language){
        LocaleHelper.setLocale(this.getActivity(),language);
        languageSelectorInterface.languageChanged();
    }
}
