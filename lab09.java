import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static java.awt.Font.*;
import static javafx.scene.text.Font.font;
import javafx.scene.chart.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class lab09 extends Application
{
    private Canvas canvas;
    private GraphicsContext gc;
    private float yMax;
    private float yMin;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Group root = new Group();
        Scene scene = new Scene(root, 600, 600);
        canvas = new Canvas();
        canvas.widthProperty().bind(primaryStage.widthProperty());
        canvas.heightProperty().bind(primaryStage.heightProperty());
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        primaryStage.setTitle("lab09");
        primaryStage.setScene(scene);
        primaryStage.show();

        drawLinePlot();
    }

    private float[] downloadStockPrices(String stock, LocalDateTime start, LocalDateTime end)
    {
        String urlBase = "https://query1.finance.yahoo.com/v7/finance/download/" + stock + "?period1=" + String.valueOf(start.toEpochSecond(ZoneOffset.UTC)) + "&period2=" + String.valueOf(end.toEpochSecond(ZoneOffset.UTC)) + "&interval=1wk&events=history&includeAdjustedClose=true";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlBase)).build();
        HttpResponse<String> response = null;
        try
        {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (Exception e)
        {
            System.out.println("IOException Error!");
        }
        String[] lines = response.body().split("\n");
        float[] prices = new float[lines.length - 1];
        for (int i = 1; i < lines.length; i++)
        {
            prices[i-1] = Float.parseFloat(lines[i].split(",")[5]);
        }
        return prices;
    }

    private void plotLine(float[] stockPrice, Color color)
    {
        gc.setStroke(color);
        int prevX = 50;
        float norm = (stockPrice[0] - yMin) / (yMax - yMin);
        float prevY = 500 - (norm * (450 - 50));
        float incX = (500 - 50) / stockPrice.length;
        for (int i = 1; i < stockPrice.length; i++)
        {
            float num = (stockPrice[i] - yMin) / (yMax - yMin); //normalize
            float yPos = 500 - (num * (450 - 50));              //denormalize
            gc.strokeLine(prevX, prevY, (int)(prevX + incX), (int)yPos);
            prevX = prevX + (int)incX;
            prevY = (int)yPos;
        }
    }

    private void drawLinePlot()
    {
        float[] adjClose1 = downloadStockPrices("TSLA", LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2021, 1, 1, 0, 0));
        float[] adjClose2 = downloadStockPrices("AMZN", LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2021, 1, 1, 0, 0));

        yMax = Float.MIN_VALUE;
        yMin = Float.MAX_VALUE;
        for (int i = 0; i < adjClose1.length - 1; i++)
        {
            yMax = Float.max(yMax, adjClose1[i]);
            yMax = Float.max(yMax, adjClose2[i]);

            yMin = Float.min(yMin, adjClose1[i]);
            yMin = Float.min(yMin, adjClose2[i]);
        }

        gc.setStroke(Color.BLACK);
        gc.strokeLine(50, 500, 450, 500);
        gc.strokeLine(50, 500, 50, 50);

        plotLine(adjClose1, Color.RED);
        plotLine(adjClose2, Color.BLUE);
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}