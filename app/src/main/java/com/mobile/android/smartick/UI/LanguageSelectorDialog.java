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

        return rootView;
    }
    private void setLanguage(String language){
        LocaleHelper.setLocale(this.getActivity(),language);
        languageSelectorInterface.languageChanged();
    }
}
