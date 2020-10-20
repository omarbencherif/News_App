package com.ecm2425.newsapp;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements TaskCompleted, CardAdapter.ListItemClickListener, CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<Article> articleList;
    private CardAdapter cardAdapter;
    private RecyclerView recyclerView;
    private HorizontalScrollView scrollBar;
    private SwipeRefreshLayout swipeRefresh;
    //The ID of the category which is checked in the RadioGroup
    private int checkedID;
    private File cacheFile;


    MainActivity() {
        //Creates a news feed and executes it
        RetrieveNewsFeed myFeed = new RetrieveNewsFeed(this, "general");
        myFeed.execute();
    }

    /**
     * When the MainActivity is created, it assigns every view in the layout to a variable so that it can be programmatically manipulated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv_numbers);
        scrollBar = findViewById(R.id.scrollBar);
        LinearLayout buttonsList = (LinearLayout) scrollBar.getChildAt(0);
        swipeRefresh = findViewById(R.id.swiperefresh);
        checkedID = findViewById(R.id.general).getId();
        //The cacheFile is declared as a file in the internal storage called cacheFile.txt
        cacheFile = new File(getFilesDir().toString() + "/cacheFile.txt");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        swipeRefresh.setOnRefreshListener(this);
        scrollBar.bringToFront();


    }

    @Override
    public void taskCompleted() {
        //Once it has the callback, it creates a CardAdapter and begins to assign cards to the RecyclerView
        cardAdapter = new CardAdapter(articleList, this);
        recyclerView.setAdapter(cardAdapter);
        //This adds dividers to the cards in order to separate them and see their boundaries.
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

    }

    /**
     * When a radio button is clicked, it changes the type of news which is being shown depending on which one is clicked.
     */
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        RetrieveNewsFeed myFeed = null;

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.general:
                if (checked)
                    myFeed = new RetrieveNewsFeed(this, "general");
                break;
            case R.id.entertainment:
                if (checked)
                    myFeed = new RetrieveNewsFeed(this, "entertainment");
                break;
            case R.id.sports:
                if (checked)
                    myFeed = new RetrieveNewsFeed(this, "sports");
                break;
            case R.id.technology:
                if (checked)
                    myFeed = new RetrieveNewsFeed(this, "technology");
                break;
            case R.id.health:
                if (checked)
                    myFeed = new RetrieveNewsFeed(this, "health");
                break;
            case R.id.science:
                if (checked)
                    myFeed = new RetrieveNewsFeed(this, "science");
                break;
            case R.id.business:
                if (checked)
                    myFeed = new RetrieveNewsFeed(this, "business");
                break;
        }
        checkedID = view.getId();
        myFeed.execute();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onRefresh() {
        RetrieveNewsFeed myFeed = new RetrieveNewsFeed(this, findViewById(checkedID).getResources().getResourceEntryName(checkedID));
        myFeed.execute();
        swipeRefresh.setRefreshing(false);
    }


    /**
     * This class is an object which creates a list of Articles after fetching them from the API
     */
    class RetrieveNewsFeed extends AsyncTask<Void, Void, String> {
        private TaskCompleted ne;
        //gotNews is necessary for the callback to work
        private boolean gotNews;
        private String category;

        RetrieveNewsFeed(TaskCompleted event, String newsType) {
            ne = event;
            gotNews = false;
            this.category = newsType;

        }

        protected void onPreExecute() {
        }

        protected String doInBackground(Void... urls) {
            return getNewsString();
        }

        /** Makes a http connection in order to get the article data from the API and saves it to a string.
         * @return a string with all of the news information
         */
        private String getNewsString() {
            try {
                //The URL can change depending on which radio button is checked
                URL url = new URL(String.format("https://newsapi.org/v2/top-headlines?country=gb&category=%s&pageSize=50&apiKey=0e999ae511e84e169d67a8fce5475517", category));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        /** After execution, the news feed checks if there is a response. If there is not, it loads a response from cache.
         * If there is, it writes the result to cache
         * It then turns it into a JSONObject as it is easier to manipulate and it is turned into a list of articles.
         * @param response
         */
        protected void onPostExecute(String response) {
            articleList = new ArrayList<>();

            if (response == null) {
                response = getResponseFromCache();
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setMessage("Failed to fetch new news stories. Please check your internet connection. Loading last session from cache.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();


            } else {
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile), 1024);
                    out.write(response);
                    out.close();
                } catch (IOException e) {
                    Log.e("Failed to cache data.", e.getMessage(), e);
                }
            }


            JSONObject jsonResult;

            try {
                jsonResult = new JSONObject(response);
                makeArticleList(jsonResult);


            } catch (
                    JSONException e) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setMessage("Failed to fetch new news stories. Please check your internet connection.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }

            //Makes a callback when the task is completed
            ne.taskCompleted();

        }

        /** Loads the response from the cache file.
         * @return a response
         */
        private String getResponseFromCache() {
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            try {
                BufferedReader cacheReader = new BufferedReader(new FileReader(cacheFile));
                while ((line = cacheReader.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }
                cacheReader.close();

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
            }
            return responseBuilder.toString();
        }

        /** Turns the JSONObject with the response into a JSONArray and then splits the JSONArray into
         * articles and adds them to articleList.
         * @param jsonResult
         * @throws JSONException
         */
        private void makeArticleList(JSONObject jsonResult) throws JSONException {
            JSONArray jsonArticles;
            JSONObject jsonSourceInfo;
            jsonArticles = jsonResult.getJSONArray("articles");

            if (jsonArticles != null) {
                for (int i = 0; i < jsonArticles.length(); i++) {
                    //The individual fields are extracted from the JSONArray
                    String headline = (jsonArticles.getJSONObject(i).getString("title"));
                    String URL = (jsonArticles.getJSONObject(i).getString("url"));

                    jsonSourceInfo = (jsonArticles.getJSONObject(i).getJSONObject("source"));
                    String source = jsonSourceInfo.getString("name");

                    String pictureURL = (jsonArticles.getJSONObject(i).getString("urlToImage"));

                    articleList.add(new Article(headline, URL, source, pictureURL));

                }
            }

        }
    }
}


