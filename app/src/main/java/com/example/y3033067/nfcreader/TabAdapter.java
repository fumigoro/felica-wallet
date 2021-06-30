package com.example.y3033067.nfcreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

public class TabAdapter extends FragmentPagerAdapter {
    final String[] pageTitle = {"読み取り", "表示", "マイページ"};

    public TabAdapter(@NonNull @NotNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @NotNull
    @Override
    public Fragment getItem(int position){
        switch(position){
            case 0:
                return new TabReadFragment();
            case 1:
                return new TabShowFragment();
            case 2:
                return new TabMyPageFragment();
        }
        return null;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        return pageTitle[position];
    }

    @Override
    public int getCount() {
        return 3;
    }
}
