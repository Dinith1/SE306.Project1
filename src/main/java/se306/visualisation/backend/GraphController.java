package se306.visualisation.backend;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSource;
import org.graphstream.ui.layout.*;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.swingViewer.Viewer;
import se306.input.CommandLineParser;
import se306.input.InputFileReader;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class GraphController {

    @FXML
    ImageView graph;

    @FXML
    Label timeElapsed, numberOfNodes, nodesToSchedule;

    public void createGraph() throws IOException {
        // System.out.println(graph);

        CommandLineParser parser = CommandLineParser.getInstance();

        // String filePath = parser.getInputFileName();
        // Graph g = new DefaultGraph("g");
        // FileSource fs = FileSourceFactory.sourceFor(filePath);

        // GraphStream
        // fs.addSink(g);

        // try {
        //     fs.begin(filePath);

        //     while (fs.nextEvents()) {
        //         // Optionally some code here ...
        //     }
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }

        // try {
        //     fs.end();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // } finally {
        //     fs.removeSink(g);
        // }

        Graph g = new DefaultGraph("g");

        double x = 0, y = 0, z = 0;

        for (se306.input.Node n : InputFileReader.listOfSortedNodesStatic) {
            Node node = g.addNode(n.getNodeIdentifier());
            node.setAttribute("label", n.getNodeIdentifier());
            node.setAttribute("xyx", new double[]{x, y, z});
            x += 1;
            y += 1;
            // z += 1;
        }

        for (se306.input.Edge e : InputFileReader.listOfEdgesStatic) {
            String startNode = e.getNodeStart().getNodeIdentifier();
            String endNode = e.getNodeEnd().getNodeIdentifier();
            g.addEdge(startNode + endNode, startNode, endNode, true);
        }

        Iterable<Node> ite = (Iterable<Node>) g.getEachNode();

        for (Node n : ite) {
            Iterable<String> attr = n.getAttributeKeySet();

            for (String s : attr) {
                Object o = n.getAttribute(s);
                System.out.println(s + ": " + o + "\n");
            }

        }

//        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        FileSinkImages pic = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.HD1080);
//        pic.setRenderer(FileSinkImages.RendererType.SCALA);
        pic.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_AT_NEW_IMAGE);

        try {
            // g.setAttribute("ui.stylesheet", styleSheet);
            pic.writeAll(g, "sample.png");
            File file = new File("sample.png");
            Image image = new Image(file.toURI().toString());
            System.out.println("\n\n\n");
            System.out.println(image);

            graph.setImage(image);
            System.out.println("\n\n\n");


            System.out.println(graph);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }

        setNumberOfNodes(Integer.toString(InputFileReader.numNodes));
    }

    public void setTimeElapsed(String newText) {
        timeElapsed.setText(newText);
    }

    public void setNumberOfNodes(String s) {
        numberOfNodes.setText(s);
    }

    public void setNodesToSchedule(String newText) {
        nodesToSchedule.setText(newText);
    }

    private String styleSheet = ""
            + "graph {"
            + "	canvas-color: white; "
            + "	fill-mode: gradient-radial; "
            + "	fill-color: white, #EEEEEE; "
            + "	padding: 60px; "
            + "}"
            + ""
            + "node {"
            + "	shape: freeplane;"
            + "	size: 100px;"
            + "	size-mode: fit;"
            + "	fill-mode: none;"
            + "	stroke-mode: plain;"
            + "	stroke-color: blue;"
            + "	stroke-width: 3px;"
            + "	padding: 5px, 1px;"
            + "	shadow-mode: none;"
            + "	icon-mode: at-left;"
            + "	text-style: normal;"
            + "	text-font: 'Droid Sans';"
            + "}"
            + ""
            + "node:clicked {"
            + "	stroke-mode: plain;"
            + "	stroke-color: red;"
            + "}"
            + ""
            + "node:selected {"
            + "	stroke-mode: plain;"
            + "	stroke-color: blue;"
            + "}"
            + ""
//            + "sprite {"
//            + " sprite-orientation: "
//            + "}"
//            + ""
            + "edge {"
            + "	shape: freeplane;"
            + "	size: 10px;"
            + "	fill-color: grey;"
            + "	fill-mode: plain;"
            + "	shadow-mode: none;"
            + "	shadow-color: rgba(0,0,0,100);"
            + "	shadow-offset: 3px, -3px;"
            + "	shadow-width: 0px;"
            + "	arrow-shape: arrow;"
            + "	arrow-size: 100px, 20px;"
            + "}"
            ;
}
