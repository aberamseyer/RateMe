package com.abe.jason.rateme.activity;

import android.graphics.Color;
import android.os.Bundle;

import com.abe.jason.rateme.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class Introduction extends AppIntro {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int text_color = getResources().getColor(R.color.textColor);
        SliderPage sliderPage1 = new SliderPage();
        sliderPage1.setDescColor(text_color);
        sliderPage1.setTitleColor(text_color);
        sliderPage1.setTitle("Welcome to RateMe!");
        sliderPage1.setDescription(getResources().getString(R.string.app_intro));
        sliderPage1.setImageDrawable(R.drawable.five_stars);
        sliderPage1.setBgColor(getResources().getColor(R.color.darker));
        addSlide(AppIntroFragment.newInstance(sliderPage1));

        SliderPage sliderPage2 = new SliderPage();
        sliderPage2.setDescColor(text_color);
        sliderPage2.setTitleColor(text_color);
        sliderPage2.setTitle("How to Use this App");
        sliderPage2.setDescription(getResources().getString(R.string.how_to_use));
        sliderPage2.setImageDrawable(R.drawable.how_to_use);
        sliderPage2.setBgColor(getResources().getColor(R.color.darker));
        addSlide(AppIntroFragment.newInstance(sliderPage2));

        SliderPage sliderPage3 = new SliderPage();
        sliderPage3.setDescColor(text_color);
        sliderPage3.setTitleColor(text_color);
        sliderPage3.setTitle("Notice of Agreement");
        sliderPage3.setDescription(getResources().getString(R.string.facial_agreement));
        sliderPage3.setImageDrawable(R.drawable.warning);
        sliderPage3.setBgColor(getResources().getColor(R.color.darker));
        addSlide(AppIntroFragment.newInstance(sliderPage3));


        // Override bar/separator color
        setBarColor(getResources().getColor(R.color.darker));
        setSeparatorColor(text_color);
        setIndicatorColor(text_color, Color.parseColor("#ffffff"));
        setColorDoneText(text_color);
        setColorSkipButton(text_color);

        showStatusBar(false);

        // Hide Skip/Done button
        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed() {
        // Do something when users tap on Skip button.
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged() {
        // Do something when slide is changed
    }
}
