package com.project.realproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SATURDAY;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.YEAR;

public class Main_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    vacationDBManager DBmanager = null;
    private static final SimpleDateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.ENGLISH);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyyMMdd", Locale.ENGLISH);
    private static final SimpleDateFormat dateFormat_dot = new SimpleDateFormat(
            "yyyy.MM.dd", Locale.ENGLISH);
    private static final DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
    private static final DecimalFormat decimalFormat2 = new DecimalFormat("###,###,###원");
    private static String[] userColumns = new String[]{"id", "nickName", "firstDate", "lastDate",
            "mealCost", "trafficCost", "totalFirstVac", "totalSecondVac", "totalSickVac", "payDay"};
    private static String[] vacationColumns = new String[]{"id", "vacation", "startDate", "type", "count"};
    private static final int[] payDependsOnRankBefore2020 = new int[]{306100, 331300, 366200, 405700};
    private static final int[] payDependsOnRankAfter2020 = new int[]{408100, 441600, 488200, 540900};
    private static String[] listOfHoliday = new String[]{"0101", "0301", "0505", "0512", "0606", "0815",
            "1003", "1009", "1225"};
    public static Context mContext;
    private String firstDate;
    private String lastDate;
    private String pivotDate;
    private String pivotPlusOneDate;
    private int mealCost;
    private int trafficCost;
    private int totalFirstVac;
    private int totalSecondVac;
    private int totalSickVac;
    private int payDay;
    private String searchStartDate;
    private String toolTipText;


    private TextView firstVacRemain;
    private TextView secondVacRemain;
    private TextView sickVacRemain;
    private TextView firstVacTotal;
    private TextView secondVacTotal;
    private TextView sickVacTotal;
    private TextView nickNameTextView;
    private TextView dDayTextView;
    private TextView servicePeriodTextView;
    private TextView searchPeriodTextView;
    private ImageView goToNextPeriod;
    private ImageView goToPreviousPeriod;
    private TextView salaryTextView;
    private TextView thisMonthOuting;
    private TextView thisMonthVac;
    private TextView thisMonthSickVac;
    private ProgressBar progressBar;
    private TextView progressPercentage;
    private TextView entireService;
    private TextView currentService;
    private TextView remainService;
    private ImageView nav_button;
    private ImageView cal_button;
    private ImageView toolTipRank;
    private ImageView toolTipPay;

    private CountDownTimer countDownTimer;
    private boolean timerIsRunning;
    private long entire;
    private long current;

    private LinearLayout vacCard1;
    private LinearLayout vacCard2;
    private LinearLayout vacCard3;
    private LinearLayout container;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        DBmanager = vacationDBManager.getInstance(this);
        mContext = this;

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        nav_button = findViewById(R.id.navigation_drawer_button);
        cal_button = findViewById(R.id.calculator_button);
        nickNameTextView = findViewById(R.id.tv_nickName);
        dDayTextView = findViewById(R.id.tv_dDay);
        servicePeriodTextView = findViewById(R.id.tv_servicePeriod);
        searchPeriodTextView = findViewById(R.id.tv_search_period);
        goToNextPeriod = findViewById(R.id.iv_search_next_period);
        goToPreviousPeriod = findViewById(R.id.iv_search_previous_period);
        salaryTextView = findViewById(R.id.tv_salary);
        thisMonthOuting = findViewById(R.id.thisMonthOuting);
        thisMonthVac = findViewById(R.id.thisMonthVac);
        thisMonthSickVac = findViewById(R.id.thisMonthSickVac);
        progressBar = findViewById(R.id.progressBar);
        progressPercentage = findViewById(R.id.progress_percentage);
        entireService = findViewById(R.id.tv_entireService);
        currentService = findViewById(R.id.tv_currentService);
        remainService = findViewById(R.id.tv_remainService);
        vacCard1 = findViewById(R.id.vacCardView_1);
        vacCard2 = findViewById(R.id.vacCardView_2);
        vacCard3 = findViewById(R.id.vacCardView_3);
        container = findViewById(R.id.fragment_container_1);
        toolTipRank = findViewById(R.id.btn_rank_info);
        toolTipPay = findViewById(R.id.btn_pay_info);

        Button spendButton = findViewById(R.id.spendButton_1);
        Button spendButton_2 = findViewById(R.id.spendButton_2);
        Button spendButton_3 = findViewById(R.id.spendButton_3);
        firstVacRemain = findViewById(R.id.first_vacation_remain);
        secondVacRemain = findViewById(R.id.second_vacation_remain);
        sickVacRemain = findViewById(R.id.sick_vacation_remain);
        firstVacTotal = findViewById(R.id.first_vacation_total);
        secondVacTotal = findViewById(R.id.second_vacation_total);
        sickVacTotal = findViewById(R.id.sick_vacation_total);

        vacCard1.setOnClickListener(this);
        vacCard2.setOnClickListener(this);
        vacCard3.setOnClickListener(this);
        spendButton.setOnClickListener(this);
        spendButton_2.setOnClickListener(this);
        spendButton_3.setOnClickListener(this);
        goToNextPeriod.setOnClickListener(this);
        goToPreviousPeriod.setOnClickListener(this);
        cal_button.setOnClickListener(this);


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        load();
        if (!timerIsRunning && DBmanager.getDataCount(vacationDBManager.TABLE_USER) != 0) {
            resetTimer();
            startTimer();
        }
    }

    public void load() {
        if (DBmanager.getDataCount(vacationDBManager.TABLE_USER) != 0) {
            setUserProfile();
            setRemainVac();
            setSearchStartDate();
            setThisMonthInfo(searchStartDate);
            progressBar.setProgress((int) getPercentage());

            progressPercentage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    int barWidth = progressBar.getRight() - progressBar.getLeft();
                    float position_X = (progressBar.getX() + (barWidth * getPercentage() / 100)) - 40;
                    if (position_X < 60) {
                        progressPercentage.setX(60);
                    } else if (position_X > barWidth - 230) {
                        progressPercentage.setX(barWidth - 230);
                    } else {
                        progressPercentage.setX((progressBar.getX() + (barWidth * getPercentage() / 100)) - 40);
                    }
                }
            });

        } else {
            DialogFrag_DateRevise dialog = new DialogFrag_DateRevise();
            FragmentManager fg = getSupportFragmentManager();
            dialog.show(fg, "dialog");
        }
    }

    public long getCurrentSecond() {
        try {
            return (Calendar.getInstance().getTime().getTime() - formatter.parse(firstDate).getTime()) / 50;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getEntireSecond() {
        try {
            return (formatter.parse(lastDate).getTime() - formatter.parse(firstDate).getTime()) / 50;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getPercentage() {
        return (100 * getCurrentSecond() / getEntireSecond());
    }


    public void startTimer() {
        countDownTimer = new CountDownTimer(entire, 50) {

            @Override
            public void onTick(long millisUntilFinished) {
                current++; // 0.05초 지날때마다 0.05초씩 더해주는 것과 같다.
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timerIsRunning = false;
            }
        }.start();
        timerIsRunning = true;
    }

    public void pauseTimer() {
        countDownTimer.cancel();
        timerIsRunning = false;
    }

    public void resetTimer() {
        entire = getEntireSecond();
        current = getCurrentSecond();
        updateCountDownText();
    }

    public void updateCountDownText() {
        double p = ((double) current / (double) entire) * 100;
        progressPercentage.setText(String.format("%.7f", p) + "%");
    }

    @Override
    public void onClick(View view) {

        Calendar calendar = Calendar.getInstance();
        FragmentManager fg = getSupportFragmentManager();
        FragmentTransaction ft = fg.beginTransaction();
        Frag2_save dialog;

        switch (view.getId()) {
            case R.id.vacCardView_1:
                setListView(R.id.first_vacation_image, ft,
                        firstDate, pivotDate, 1, "list1");
                break;

            case R.id.vacCardView_2:
                setListView(R.id.second_vacation_image, ft,
                        pivotPlusOneDate, lastDate, 2, "list2");
                break;

            case R.id.vacCardView_3:
                setListView(R.id.sick_vacation_image, ft,
                        firstDate, lastDate, 3, "list3");
                break;

            case R.id.spendButton_1:
                dialog = new Frag2_save().newInstance(firstDate, pivotDate, 1, searchStartDate);
                dialog.show(fg, "dialog");
                break;

            case R.id.spendButton_2:
                dialog = new Frag2_save().newInstance(pivotPlusOneDate, lastDate, 2, searchStartDate);
                dialog.show(fg, "dialog");
                break;

            case R.id.spendButton_3:
                dialog = new Frag2_save().newInstance(firstDate, lastDate, 3, searchStartDate);
                dialog.show(fg, "dialog");
                break;

            case R.id.iv_search_next_period:
                try {
                    calendar.setTime(dateFormat.parse(searchStartDate));
                    calendar.add(MONTH, 1);
                    searchStartDate = dateFormat.format(calendar.getTime());
                    if (!setThisMonthInfo(searchStartDate)) {
                        calendar.add(MONTH, -1);
                        searchStartDate = dateFormat.format(calendar.getTime());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.iv_search_previous_period:
                try {
                    calendar.setTime(dateFormat.parse(searchStartDate));
                    calendar.add(MONTH, -1);
                    searchStartDate = dateFormat.format(calendar.getTime());
                    if (!setThisMonthInfo(searchStartDate)) {
                        calendar.add(MONTH, 1);
                        searchStartDate = dateFormat.format(calendar.getTime());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.calculator_button:
                Intent calculator = new Intent(Main_Activity.this, Calculator_Activity.class);
                startActivity(calculator);
                break;
        }
    }

    public void setListView(int imageId, FragmentTransaction ft,
                            String lowerBoundDate, String upperBoundDate, int numOfYear, String tag) {
        ImageView imageView = findViewById(imageId);
        Fragment fragment;

        if (container.getVisibility() == GONE) {
            switch (tag) {
                case "list1":
                    vacCard1.setVisibility(VISIBLE);
                    vacCard2.setVisibility(GONE);
                    vacCard3.setVisibility(GONE);
                    break;
                case "list2":
                    vacCard1.setVisibility(GONE);
                    vacCard2.setVisibility(VISIBLE);
                    vacCard3.setVisibility(GONE);
                    break;
                case "list3":
                    vacCard1.setVisibility(GONE);
                    vacCard2.setVisibility(GONE);
                    vacCard3.setVisibility(VISIBLE);
                    break;
            }

            imageView.setImageResource(R.drawable.ic_expand_less_black_24dp);
            fragment = new Frag2_listview().newInstance(lowerBoundDate, upperBoundDate, firstDate,
                    lastDate, numOfYear, searchStartDate);
            ft.replace(R.id.fragment_container_1, fragment, tag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
            container.setVisibility(VISIBLE);
        } else {
            vacCard1.setVisibility(VISIBLE);
            vacCard2.setVisibility(VISIBLE);
            vacCard3.setVisibility(VISIBLE);
            container.setVisibility(GONE);
            imageView.setImageResource(R.drawable.ic_expand_more_black_24dp);
            ft.replace(R.id.fragment_container_1, new BlankFragment(), "empty");
            ft.commit();
        }
    }

    public void toolTipRank(View view) {
        try {
            if (view == toolTipRank || view == nickNameTextView) {
                view = toolTipRank;
                Calendar calendar = Calendar.getInstance();
                Calendar today = Calendar.getInstance();
                calendar.setTime(formatter.parse(firstDate));
                calendar.set(DATE, 1);
                int currentPay = payDependsOnMonth(today.getTime());
                String rank = null;

                if (currentPay == payDependsOnRankBefore2020[0]) {
                    calendar.add(MONTH, 3);
                    rank = "이등병 (2020년 개정전 기준)";
                } else if (currentPay == payDependsOnRankAfter2020[0]) {
                    calendar.add(MONTH, 2);
                    rank = "이등병 (2020년 개정후 기준)";
                } else if (currentPay == payDependsOnRankBefore2020[1]) {
                    calendar.add(MONTH, 10);
                    rank = "일병 (2020년 개정전 기준)";
                } else if (currentPay == payDependsOnRankAfter2020[1]) {
                    calendar.add(MONTH, 8);
                    rank = "일병 (2020년 개정후 기준)";
                } else if (currentPay == payDependsOnRankBefore2020[2]) {
                    calendar.add(MONTH, 17);
                    rank = "상병 (2020년 개정전 기준)";
                } else if (currentPay == payDependsOnRankAfter2020[2]) {
                    calendar.add(MONTH, 14);
                    rank = "상병 (2020년 개정후 기준)";
                } else if (currentPay == payDependsOnRankBefore2020[3]) {
                    calendar.setTime(formatter.parse(lastDate));
                    rank = "병장 (2020년 개정전 기준)";
                } else if (currentPay == payDependsOnRankAfter2020[3]) {
                    calendar.setTime(formatter.parse(lastDate));
                    rank = "병장 (2020년 개정후 기준)";
                }
                toolTipText = ("<b>계급</b> | " + rank + "<br><br>" + "<b>현재 기본급</b> | "
                        + decimalFormat.format(currentPay) + "원<br><br>" + "<b>다음 진급일</b> | " + dateFormat_dot.format(calendar.getTime()) + "");
            } else if (view == toolTipPay || view == salaryTextView) {
                view = toolTipPay;
                setThisMonthInfo(searchStartDate);
            }

            Tooltip toolTip = new Tooltip.Builder(this)
                    .styleId(R.style.ToolTipLayoutCustomStyle)
                    .text(toolTipText)
                    .anchor(view, 0, 0, false) //follow 뭔지알아보기
                    .activateDelay(0)
                    .showDuration(10000)
                    .closePolicy(new ClosePolicy.Builder()
                            .inside(true)
                            .outside(true)
                            .build())
                    .arrow(true)
                    .create();
            if(view == toolTipRank) {
                toolTip.show(view, Tooltip.Gravity.RIGHT, true);
            } else if(view == toolTipPay){
                toolTip.show(view, Tooltip.Gravity.LEFT, true);
            }

        } catch (ParseException e) {
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        DialogFrag_DateRevise dialog = new DialogFrag_DateRevise();
        FragmentManager fg = getSupportFragmentManager();
        switch (menuItem.getItemId()) {
            case R.id.profile:
                dialog.show(fg, "dialog");
                if (timerIsRunning) {
                    pauseTimer();
                }
                break;
            case R.id.manual:
                Intent manual = new Intent(Main_Activity.this, Manual_Activity.class);
                startActivity(manual);
                break;
            case R.id.calculator:
                Intent calculator = new Intent(Main_Activity.this, Calculator_Activity.class);
                startActivity(calculator);
                break;
            case R.id.feedback:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/Text");
                email.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"alleyoops.app@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report));
                startActivity(email);
                break;
            case R.id.rating:
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            case R.id.reset:
                new AlertDialog.Builder(this)
                        .setMessage(R.string.reset_alert)
                        .setCancelable(false)
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                DBmanager.deleteAll();
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                new DialogFrag_DateRevise().show(fragmentManager, "dialog");
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
        }
        return false;
    }

    public void setUserProfile() {
        if (DBmanager.getDataCount(vacationDBManager.TABLE_USER) != 0) {
            Cursor c = DBmanager.query(userColumns, vacationDBManager.TABLE_USER,
                    null, null, null, null, null);
            c.moveToFirst();
            firstDate = c.getString(2);
            lastDate = c.getString(3);
            mealCost = c.getInt(4);
            trafficCost = c.getInt(5);
            totalFirstVac = c.getInt(6);
            totalSecondVac = c.getInt(7);
            totalSickVac = c.getInt(8);
            payDay = c.getInt(9);

            nickNameTextView.setText(c.getString(1));
            servicePeriodTextView.setText(firstDate + " ~ " + lastDate);
            countDdayFromToday();

            firstVacTotal.setText(" / " + totalFirstVac);
            secondVacTotal.setText(" / " + totalSecondVac);
            sickVacTotal.setText(" / " + totalSickVac);

            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(formatter.parse(firstDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            cal.add(YEAR, 1);
            Date pivotTime = cal.getTime();
            cal.add(DATE, 1);
            Date pivotPlusOneTime = cal.getTime();
            pivotDate = formatter.format(pivotTime);
            pivotPlusOneDate = formatter.format(pivotPlusOneTime);
            c.close();
        }
    }

    public int payDependsOnMonth(Date searchDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(searchDate);
        int firstYear = Integer.parseInt(firstDate.substring(0, 4));
        int firstMonth = Integer.parseInt(firstDate.substring(5, 7));
        int year = calendar.get(YEAR);
        int month = calendar.get(MONTH) + 1;
        int diff = ((year - firstYear) * 12) + (month - firstMonth);

        // 2020년을 기준으로도 알아야함
        if (year <= 2019) {
            if (diff < 3) return payDependsOnRankBefore2020[0];
            else if (diff >= 3 && diff < 10) return payDependsOnRankBefore2020[1];
            else if (diff >= 10 && diff < 17) return payDependsOnRankBefore2020[2];
            else return payDependsOnRankBefore2020[3];
        } else {
            if (diff < 2) return payDependsOnRankAfter2020[0];
            else if (diff >= 2 && diff < 8) return payDependsOnRankAfter2020[1];
            else if (diff >= 8 && diff < 14) return payDependsOnRankAfter2020[2];
            else return payDependsOnRankAfter2020[3];
        }
    }

    public void setRemainVac() {
        Cursor c = DBmanager.query(vacationColumns, vacationDBManager.TABLE_FIRST,
                null, null, null, null, null);
        try {
            double firstCount = (double) totalFirstVac * 480;
            double secondCount = (double) totalSecondVac * 480;
            double thirdCount = (double) totalSickVac * 480;
            long firstTime = formatter.parse(firstDate).getTime();
            long lastTime = formatter.parse(lastDate).getTime();
            long pivotTime = formatter.parse(pivotDate).getTime();
            while (c.moveToNext()) {
                String startDate = c.getString(2);
                String type = c.getString(3);
                long startTime = formatter.parse(startDate).getTime();
                if (type.equals("병가") || type.equals("오전지참")
                        || type.equals("오후조퇴") || type.equals("병가외출")) {
                    thirdCount -= c.getDouble(4);
                } else if (startTime - firstTime >= 0 && pivotTime - startTime >= 0) {
                    firstCount -= c.getDouble(4);
                } else if (startTime - pivotTime > 0 && lastTime - startTime >= 0) {
                    secondCount -= c.getDouble(4);
                }
            }
            c.close();
            firstVacRemain.setText(convertMinuteToProperUnit((int) firstCount));
            secondVacRemain.setText(convertMinuteToProperUnit((int) secondCount));
            sickVacRemain.setText(convertMinuteToProperUnit((int) thirdCount));
        } catch (ParseException e) {
        }
    }


    public boolean setThisMonthInfo(String searchDate) {
        int first, last, entireLength;
        Calendar cal = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(searchDate));
            int year = cal.get(YEAR);
            int month = cal.get(MONTH) + 1;
            int tmp = (year * 10000) + (month * 100) + (payDay);
            c.set(year, month - 1, payDay);
            Date compareDate = dateFormat.parse(tmp + "");
            long diff = cal.getTimeInMillis() - compareDate.getTime();
            // 이번달 월급날이 지난것
            if (diff >= 0) {
                c.add(MONTH, 1);
                c.add(DATE, -1);
                first = tmp;
                last = ((c.get(YEAR) * 10000) + ((c.get(MONTH) + 1) * 100)) + c.get(DATE);
                entireLength = getDateLength(first, last);
                first = checkFirstDayInclude(first);
                last = checkLastDayInclude(last);
            }
            // 이번달 월급날이 지나지 않은것
            else {
                c.add(MONTH, -1);
                first = ((c.get(YEAR) * 10000) + ((c.get(MONTH) + 1) * 100)) + c.get(DATE);
                c.add(MONTH, 1);
                c.add(DATE, -1);
                last = ((c.get(YEAR) * 10000) + ((c.get(MONTH) + 1) * 100)) + c.get(DATE);
                entireLength = getDateLength(first, last);
                first = checkFirstDayInclude(first);
                last = checkLastDayInclude(last);
            }
            if (last - first >= 0) {
                searchPeriodTextView.setText(intToDateFormatString(first) + " ~ " + intToDateFormatString(last));
                setThisMonthSpendVac(first, last, entireLength);
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setThisMonthSpendVac(int first_tmp, int last_tmp, int entireLength) throws ParseException {
        // 소집일 소집해제일이 끼어있는지 주의
        Calendar cal = Calendar.getInstance();
        double sum_sickVac = 0;
        double sum_Vac = 0;
        double sum_outing = 0;
        // 출근횟수 구하기
        int numberOfEntire; // 전체 날짜수
        int numberOfWork; // 출근횟수 넣기 (토, 일, 공휴일 빼고 카운트)
        int numberOfMeal;
        int numberOfTraffic;
        double pay = 0;
        boolean isPromoted = false;
        boolean isAmended = false;
        int payBeforePromotion;
        int payAfterPromotion = 0;
        int numberOfAfterPromotion = 0;

        Date first_payDate = dateFormat.parse(first_tmp + "");
        Date last_payDate = dateFormat.parse(last_tmp + "");
        numberOfEntire = getDateLength(first_tmp, last_tmp);
        numberOfWork = numberOfEntire;
        Date searchDate = first_payDate;
        cal.setTime(searchDate);

        payBeforePromotion = payDependsOnMonth(searchDate);
        double checkPromotion = (double) (payDependsOnMonth(searchDate) / entireLength);
        int checkYear = cal.get(YEAR);

        while (searchDate.compareTo(last_payDate) <= 0) {
            cal.setTime(searchDate);
            String onlyMonthAndDate = dateFormat.format(searchDate).substring(4);

            if (cal.get(Calendar.DAY_OF_WEEK) == SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == SUNDAY ||
                    Arrays.asList(listOfHoliday).contains(onlyMonthAndDate)) {
                numberOfWork--;
            }

            double payPerDay = (double) (payDependsOnMonth(searchDate) / entireLength);
            int year = cal.get(YEAR);
            if (checkPromotion != payPerDay) {
                if (checkYear == 2019 && year == 2020) {
                    isAmended = true;
                } else {
                    isPromoted = true;
                }
                payAfterPromotion = payDependsOnMonth(searchDate);
                numberOfAfterPromotion++;
            }

            pay += payPerDay;
            cal.add(DATE, 1);
            searchDate = cal.getTime();
        }

        numberOfMeal = numberOfWork;
        numberOfTraffic = numberOfWork;

        Cursor c = DBmanager.query(vacationColumns, vacationDBManager.TABLE_FIRST, null, null, null, null, null);
        while (c.moveToNext()) {
            String type = c.getString(3);
            double count = c.getDouble(4);
            Date startDate = formatter.parse(c.getString(2));
            long lowerDiff = startDate.getTime() - first_payDate.getTime();
            long upperDiff = last_payDate.getTime() - startDate.getTime();
            if (lowerDiff >= 0 && upperDiff >= 0) {
                if (type.equals("연가")) {
                    numberOfMeal--;
                    numberOfTraffic--;
                    sum_Vac += count;
                } else if (type.equals("오전반가")) {
                    numberOfMeal--;
                    sum_Vac += count;
                } else if (type.equals("오후반가")) {
                    sum_Vac += count;
                } else if (type.equals("외출")) {
                    sum_outing += count;
                } else if (type.equals("병가")) {
                    numberOfMeal--;
                    numberOfTraffic--;
                    sum_sickVac += count;
                } else if (type.equals("오전지참")) {
                    numberOfMeal--;
                    sum_sickVac += count;
                } else if (type.equals("오후조퇴") || type.equals("병가외출")) {
                    sum_sickVac += count;
                }
            }
        }

        thisMonthVac.setText(convertMinuteToProperUnit((int) sum_Vac));
        thisMonthSickVac.setText(convertMinuteToProperUnit((int) sum_sickVac));
        thisMonthOuting.setText(convertMinuteToProperUnit((int) sum_outing));
        int sumOfPay = (int) Math.round((pay + (mealCost * numberOfMeal) + (trafficCost * numberOfTraffic)) / 100.0) * 100;
        salaryTextView.setText(decimalFormat.format(sumOfPay) + " KRW");
        int sumOfMeal = mealCost * numberOfMeal;
        int sumOfTraffic = trafficCost * numberOfTraffic;
        int sumOfBeforePromotion = payBeforePromotion * (numberOfEntire - numberOfAfterPromotion) / entireLength;
        int sumOfAfterPromotion = payAfterPromotion * numberOfAfterPromotion / entireLength;
        int sumOfNoPromotion = payBeforePromotion * numberOfEntire / entireLength;

        if (isAmended) {
            toolTipText = ("<b>2019년 개정전</b><br>" + decimalFormat2.format(sumOfBeforePromotion) + " <b>|</b> "
                    + decimalFormat2.format(payBeforePromotion) + " x " + "(" + (numberOfEntire - numberOfAfterPromotion)
                    + "/" + entireLength + "일)" + "<br><br>" + "<b>2020년 개정후</b><br>" + decimalFormat2.format(sumOfAfterPromotion)
                    + " <b>|</b> " + decimalFormat2.format(payAfterPromotion) + " x " + "(" + (numberOfAfterPromotion)
                    + "/" + entireLength + "일)" + "<br><br>" + "<b>식비</b><br>" + decimalFormat2.format(sumOfMeal) + " <b>|</b> "
                    + decimalFormat2.format(mealCost) + " x " + numberOfMeal + "일" + "<br><br>" + "<b>교통비</b><br>" + decimalFormat2.format(sumOfTraffic) + " <b>|</b> "
                    + decimalFormat2.format(trafficCost) + " x " + numberOfTraffic + "일" + "<br><br>"
                    + "공휴일 출근횟수에서 제외");
        } else if (isPromoted) {
            toolTipText = ("<b>진급 전</b><br>" + decimalFormat2.format(sumOfBeforePromotion) + " <b>|</b> "
                    + decimalFormat2.format(payBeforePromotion) + " x " + "(" + (numberOfEntire - numberOfAfterPromotion)
                    + "/" + entireLength + "일)" + "<br><br>" + "<b>진급 후</b><br>" + decimalFormat2.format(sumOfAfterPromotion)
                    + " <b>|</b> " + decimalFormat2.format(payAfterPromotion) + " x " + "(" + (numberOfAfterPromotion)
                    + "/" + entireLength + "일)" + "<br><br>" + "<b>식비</b><br>" + decimalFormat2.format(sumOfMeal) + " <b>|</b> "
                    + decimalFormat2.format(mealCost) + " x " + numberOfMeal + "일" + "<br><br>" + "<b>교통비</b><br>" + decimalFormat2.format(sumOfTraffic) + " <b>|</b> "
                    + decimalFormat2.format(trafficCost) + " x " + numberOfTraffic + "일" + "<br><br>"
                    + "공휴일 출근횟수에서 제외");
        } else {
            toolTipText = ("<b>기본급여</b><br>" + decimalFormat2.format(sumOfNoPromotion) + " <b>|</b> " + decimalFormat2.format(payBeforePromotion) + " x "
                    + "(" + numberOfEntire + "/" + entireLength + "일)" + "<br><br>" + "<b>식비</b><br>" + decimalFormat2.format(sumOfMeal) + " <b>|</b> "
                    + decimalFormat2.format(mealCost) + " x " + numberOfMeal + "일" + "<br><br>" + "<b>교통비</b><br>" + decimalFormat2.format(sumOfTraffic) + " <b>|</b> "
                    + decimalFormat2.format(trafficCost) + " x " + numberOfTraffic + "일" + "<br><br>"
                    + "공휴일 출근횟수에서 제외");
        }
    }

    public int getDateLength(int first, int last) {
        Date first_payDate = null;
        Date last_payDate = null;
        try {
            first_payDate = dateFormat.parse(first + "");
            last_payDate = dateFormat.parse(last + "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (int) (((last_payDate.getTime() - first_payDate.getTime()) / (24 * 60 * 60 * 1000)) + 1);
    }

    public int checkFirstDayInclude(int first) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(formatter.parse(firstDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int firstDateToInt = ((calendar.get(YEAR) * 10000) + ((calendar.get(MONTH) + 1) * 100)) + calendar.get(DATE);
        if (first - firstDateToInt > 0) {
            return first;
        } else {
            return firstDateToInt;
        }
    }

    public int checkLastDayInclude(int last) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(formatter.parse(lastDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int lastDateToInt = ((calendar.get(YEAR) * 10000) + ((calendar.get(MONTH) + 1) * 100)) + calendar.get(DATE);
        if (last - lastDateToInt < 0) {
            return last;
        } else {
            return lastDateToInt;
        }
    }

    public String intToDateFormatString(int date) {
        String tmp = Integer.toString(date);
        return tmp.substring(0, 4) + "." + tmp.substring(4, 6) + "." + tmp.substring(6);
    }

    String convertMinuteToProperUnit(int minute) {
        if (minute < 0) return "남은휴가없음";
        else {
            int day = minute / 480;
            minute %= 480;
            if (day != 0) {
                if (minute == 240) return ((double) day + 0.5) + "일";
                else {
                    int hour = minute / 60;
                    minute %= 60;
                    if (hour != 0) return minute != 0 ? day + "일 " + hour + "시간 "
                            + minute + "분" : day + "일 " + hour + "시간";
                    else return minute != 0 ? day + "일 " + minute + "분 " : day + "일";
                }
            } else {
                if (minute == 240) return "0.5일";
                else {
                    int hour = minute / 60;
                    minute %= 60;
                    if (hour != 0) return minute != 0 ? hour + "시간 "
                            + minute + "분" : hour + "시간";
                    else return minute != 0 ? minute + "분" : "0일";
                }
            }
        }
    }

    public void refreshListView(int imageId, String limitStartDate, String limitLastDate, int numOfYear, String tag) {
        FragmentManager fg = getSupportFragmentManager();
        FragmentTransaction ft = fg.beginTransaction();
        ImageView imageView = findViewById(imageId);
        imageView.setImageResource(R.drawable.ic_expand_less_black_24dp);
        Frag2_listview fragment = new Frag2_listview().newInstance(limitStartDate, limitLastDate, firstDate,
                lastDate, numOfYear, searchStartDate);
        ft.replace(R.id.fragment_container_1, fragment, tag);
        ft.commit();
    }

    public void setSearchStartDate() {
        try {
            Date firstDay = formatter.parse(firstDate);
            Date lastDay = formatter.parse(lastDate);
            Date today = Calendar.getInstance().getTime();

            if (today.getTime() < firstDay.getTime()) {
                searchStartDate = dateFormat.format(firstDay);
            } else if (today.getTime() > lastDay.getTime()) {
                searchStartDate = dateFormat.format(lastDay);
            } else {
                searchStartDate = dateFormat.format(today);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void countDdayFromToday() {
        Calendar today = Calendar.getInstance();
        long todayTime = today.getTime().getTime();
        long lastTime = 0;
        long firstTime = 0;
        try {
            lastTime = formatter.parse(lastDate).getTime();
            firstTime = formatter.parse(firstDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long count = (lastTime - todayTime);
        if (count > 0) {
            dDayTextView.setText("D-" + ((count / (60 * 60 * 24 * 1000)) + 1));
        } else {
            dDayTextView.setText("소집해제");
        }
        entireService.setText((((lastTime - firstTime) / (60 * 60 * 24 * 1000))) + " 일");
        currentService.setText((((todayTime - firstTime) / (60 * 60 * 24 * 1000))) + " 일");
        remainService.setText(((count / (60 * 60 * 24 * 1000))) + 1 + " 일");
    }
}