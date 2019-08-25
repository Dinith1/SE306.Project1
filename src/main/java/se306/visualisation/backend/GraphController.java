package se306.visualisation.backend;

import com.sun.management.OperatingSystemMXBean;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import se306.Main;
import se306.algorithm.Processor;
import se306.input.CommandLineParser;
import se306.input.InputFileReader;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.net.URL;
import java.util.*;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;

public class GraphController implements Initializable {

    @FXML
    ImageView graphImage;

    @FXML
    Label timeElapsed, numberOfNodes;

    @FXML
    CategoryAxis cpu;

    @FXML
    NumberAxis cpuId;

    @FXML
    private AnchorPane schedulePane;

    @FXML
    private Button startBtn;

    @FXML
    private Tile cpuUsage, memoryUsage;

    Timeline countProgress = new Timeline();
    private static final double STARTTIME = 0;
    private final DoubleProperty seconds = new SimpleDoubleProperty(STARTTIME);



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateTile();
        timeElapsed.textProperty().bind(((seconds.divide(1000.00)).asString()));
        CommandLineParser parser = CommandLineParser.getInstance();
        if (!parser.wantVisual()) {
            Main.startScheduling();
        }
    }
    private void updateTime(){
        double seconds = this.seconds.get();
        this.seconds.set(seconds+1);
    }

    @FXML
    void handleStart(MouseEvent event) {
        Task<Void> schedule = new Task<Void>() {
            @Override
            public Void call(){
                Main.startScheduling();
                return null;
            }
        };
        schedule.setOnSucceeded(e -> { //Once tasks finished then it should re enable buttons
            countProgress.stop();
            createSchedule();
        });

        startTimer();
        new Thread(schedule).start();
        countProgress.play();
        startBtn.setDisable(true);
    }

    private void startTimer(){
        countProgress = new Timeline(new KeyFrame(Duration.millis(1),evt-> updateTime()));
        countProgress.setCycleCount((Animation.INDEFINITE));
        seconds.set(STARTTIME);
    }

    public void createGraph(MutableGraph graph) throws IOException {

        File file = new File("temp-graph.png");
        Graphviz.fromGraph(graph).width(1200).render(Format.PNG).toFile(file);

        Image image = new Image(file.toURI().toString());
        setNumberOfNodes(Integer.toString(GraphParser.totalNodes));
        graphImage.setImage(image);
    }

    public void createSchedule() {
        CommandLineParser parser = CommandLineParser.getInstance();
        String[] processors = new String[parser.getNumberOfProcessors()];
        for (int i = 0; i < parser.getNumberOfProcessors(); i++) {
            processors[i] = "Processor" + (i);
        }

        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();

        final SchedulesBar<Number,String> chart = new SchedulesBar<>(xAxis,yAxis);
        xAxis.setLabel("");
        xAxis.setTickLabelFill(Color.CHOCOLATE);
        xAxis.setMinorTickCount(4);

        yAxis.setLabel("");
        yAxis.setTickLabelFill(Color.CHOCOLATE);
        yAxis.setTickLabelGap(10);
        yAxis.setCategories(FXCollections.<String>observableArrayList(Arrays.asList(processors)));

        chart.setTitle("Final schedule");
        chart.setLegendVisible(false);
        chart.setBlockHeight( schedulePane.getPrefHeight() / (parser.getNumberOfProcessors() + 30));

        Collection<Processor> processorList = ScheduleParser.getInstance().getProcessorList();

        int i = 0;
        boolean isBlue = true;
        for (Processor p : processorList) {
            XYChart.Series series = new XYChart.Series();
            for (Integer j : p.getScheduledNodes()) {
                if (isBlue) {
                    isBlue = false;
                    series.getData().add(new XYChart.Data(p.getStartTimes().get(j), processors[i], new SchedulesBar.ExtraData(InputFileReader.nodeWeights.get(j), "status-blue")));
                } else {
                    isBlue = true;
                    series.getData().add(new XYChart.Data(p.getStartTimes().get(j), processors[i], new SchedulesBar.ExtraData(InputFileReader.nodeWeights.get(j), "status-red")));
                }
            }
        i++;
        chart.getData().add(series);
        }

        chart.getStylesheets().add(getClass().getResource("/schedule.css").toExternalForm());
        schedulePane.setLeftAnchor(chart, 0.0);
        schedulePane.setRightAnchor(chart, 0.0);
        schedulePane.setTopAnchor(chart, 0.0);
        schedulePane.setBottomAnchor(chart, 0.0);

        schedulePane.getChildren().add(chart);
    }

    public void setNumberOfNodes(String s) {
        numberOfNodes.setText(s);
    }



    private void populateTile() {
//        ChartData smoothChartData1 = new ChartData("Item 1", new Random().nextDouble() * 25, Tile.BLUE);
//        ChartData smoothChartData2 = new ChartData("Item 2", new Random().nextDouble() * 25, Tile.BLUE);
//        ChartData smoothChartData3 = new ChartData("Item 3", new Random().nextDouble() * 25, Tile.BLUE);
//        ChartData smoothChartData4 = new ChartData("Item 4", new Random().nextDouble() * 25, Tile.BLUE);
//        ChartData smoothChartData5 = new ChartData("Item 1", new Random().nextDouble() * 25, Tile.BLUE);
//        ChartData smoothChartData6 = new ChartData("Item 2", new Random().nextDouble() * 25, Tile.BLUE);
//        ChartData smoothChartData7 = new ChartData("Item 3", new Random().nextDouble() * 25, Tile.BLUE);
//        ChartData smoothChartData8 = new ChartData("Item 4", new Random().nextDouble() * 25, Tile.BLUE);
        cpuUsage.setSkinType(Tile.SkinType.SMOOTH_AREA_CHART);
        cpuUsage.setTitle("CPU Usage");
        cpuUsage.isAnimated();
        memoryUsage.setSkinType(Tile.SkinType.SMOOTH_AREA_CHART);
        memoryUsage.setTitle("Memory Usage");
        memoryUsage.isAnimated();

        List<ChartData> cpuUsageData = new LinkedList<>();
        List<ChartData> memoryUsageData = new LinkedList<>();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override public void run() {
                long currentMemoryUsage = memoryMXBean.getHeapMemoryUsage().getUsed() / 1000000;
                ((LinkedList<ChartData>) memoryUsageData).addFirst(new ChartData("Item 1", currentMemoryUsage, Tile.BLUE));
                ((LinkedList<ChartData>) cpuUsageData).addFirst(new ChartData("Item 1", new Random().nextDouble() * 25, Tile.BLUE));
                if (cpuUsageData.size() > 20) {
                    ((LinkedList<ChartData>) cpuUsageData).removeLast();
                    ((LinkedList<ChartData>) memoryUsageData).removeLast();
                }
                Platform.runLater(() -> cpuUsage.setChartData(cpuUsageData));
                Platform.runLater(() -> memoryUsage.setChartData(memoryUsageData));

            }
        }, 0L, 200L);


    }

}
