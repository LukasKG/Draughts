import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.LineTo; 
import javafx.scene.shape.MoveTo; 
import javafx.scene.shape.Path;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.WindowEvent;
import javafx.util.Duration; 

/**
 *  This class is the main class of the draughts game. 
 * 
 *  To play this game, right-click this class -> "Run JavaFX Application".
 * 
 *  This main class creates and initialises the player class 
 *  and starts the game.
 * 
 * @author  Lukas Gunthermann
 * @version 1.0
 */
public class UI extends Application
{
    public static final String pathImg = "img/";    
    /**
     * Load the Image at the given path. If it can't be found, the error symbol is loaded instead.
     * 
     * @param path The path of the image.
     * @return The loaded Image.
     */
    public static final Image loadImg(String path)
    {
        Image img;
        try{
            FileInputStream file = new FileInputStream(pathImg+path);
            if(file==null)
                img = Error.getImg();
            else
                img = new Image(file);
        }
        catch (FileNotFoundException e){
            Error.send(false,"Can't find path \""+pathImg+path+"\"\n Error: "+e);
            img = Error.getImg();
        }
        return img;
    }
    
    private static Player getPlayer(int player)
    {
        if(player==1)
            return player1;
        else
            return player2;
    }

    private static Player getOtherPlayer(int player)
    {
        return getPlayer(State.otherPlayer(player));
    }

    private static void stopPlayers()
    { 
        player1.deactivate();
        player2.deactivate();
    }

    private static Player player2; // White
    private static Player player1; // Black
    private static State gameState;

    private static Stage stage;
    private static Pane board;
    public static Background background;
    private static HashMap<Position, ImageView> pieces;

    private static ImageView p1Process;
    private static ImageView p2Process;
    private static Text p1Nodes;
    private static Text p2Nodes;

    private static Point2D originBoard = new Point2D(20,60);
    private static long AImoveTime = 400;

    // Whether colour indications on the board should be on/off
    private static boolean highlight;

    @Override
    public void start(Stage paramStage) throws Exception
    {
        // Clear Console
        System.out.print('\u000C');

        // Set stage
        stage = paramStage;

        // The application will close when the window is closed
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                quit();
            }
        });

        background = new Background(new BackgroundFill(javafx.scene.paint.Paint.valueOf("#eeeeee"),CornerRadii.EMPTY,Insets.EMPTY));

        selectPlayers();
    }

    private static void quit()
    {
        stopPlayers();
        Log.write("Game quit.");
        Platform.exit();
        System.exit(0);
    }

    private static void selectPlayers()
    {
        SelectionScreen.load(stage);
    }

    public static void restartGame()
    {
        stopPlayers();
        clearBoard();
        drawBoard();
        Thread buffer = new Thread(()-> { 
            sleep(2000);
            Platform.runLater(() -> {
                startGame(player1,player2);
            });
        });
        buffer.start();
    }
    
    public static void startGame(Player p1, Player p2)
    {
        player1 = new Player(p1);
        player2 = new Player(p2);
        gameState = new State();

        Log.write("#~~~~~~~~~~~~~~~~~~~~~~~#");
        Log.write("Player 1: "+player1.getName());
        Log.write("Player 2: "+player2.getName());
        Log.write("Game started.");
        Log.write("Board evaluation: "+Analysis.evaluate(gameState));

        loadMainScreen();
    }

    private static String getText(String path) throws FileNotFoundException, IOException
    {
        File file = new File(path);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        String s = "";
        while((line = br.readLine()) != null){
            //process the line
            s+=line+"\n";
        }
        return s;
    }

    private static void showRules()
    {
        String string = "Rules";
        try{ string = getText("rules.txt"); }
        catch(FileNotFoundException e){ Error.send(false,e.toString()); }
        catch(IOException e){ Error.send(false,e.toString()); }
        
        Stage window = new Stage();
        VBox root = new VBox();
        root.setBackground(background);
        root.setPadding(new Insets(20, 20, 20, 20));
        root.setSpacing(30);
        root.setAlignment(Pos.CENTER);
        
        Text header = new Text("Rules");
        header.setStyle("-fx-font: 24px Ebrima; -fx-font-weight: bold;");
        root.getChildren().add(header);
        
        Text text = new Text(string);
        text.setStyle("-fx-font: 18px Ebrima;");
        root.getChildren().add(text);
        
        // create scene containing the content
        Scene scene = new Scene(root,1000,800);
        text.wrappingWidthProperty().bind(scene.widthProperty().subtract(15));
        window.setScene(scene);
        window.show();
    }
    
    private static void lostGame(int turn)
    {
        updateProcess(false);
        stopPlayers();
        Player winner = getOtherPlayer(turn);
        String string = "Player "+winner.getName()+" has won the game!";
        Log.write(string);

        Stage window = new Stage();
        VBox root = new VBox();
        root.setBackground(background);
        root.setPadding(new Insets(20, 20, 20, 20));
        root.setSpacing(30);
        root.setAlignment(Pos.CENTER);
        HBox hbox = new HBox(15);
        hbox.setAlignment(Pos.CENTER);

        Text text = new Text(string);
        text.setStyle("-fx-font: 24px Ebrima; -fx-font-weight: bold;");
        root.getChildren().add(text);

        Button button1 = new Button("New Game");
        button1.setOnAction(e -> {
                window.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));
                selectPlayers();
            });

        Button button2 = new Button("Rematch");
        button2.setOnAction(e -> {
                window.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));
                startGame(player2, player1);
            });

        hbox.getChildren().addAll(button1, button2);
        root.getChildren().add(hbox);

        // create scene containing the content
        Scene scene = new Scene(root);
        window.setScene(scene);
        window.show();
    }

    private static void loadMainScreen()
    {
        // Menubar
        MenuBar menuBar = new MenuBar();
        Menu menu1 = new Menu("Game");

        MenuItem menuItem1 = new MenuItem("New Game");
        menuItem1.setOnAction(e -> {
            stopPlayers();
            selectPlayers();
        });

        MenuItem menuItem2 = new MenuItem("Restart");
        menuItem2.setOnAction(e -> {
            restartGame();
        });

        MenuItem menuItem3 = new MenuItem("Exit");
        menuItem3.setOnAction(e -> {
            quit();
            //stage.fireEvent(new WindowEvent(stage,WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        menu1.getItems().addAll(menuItem1,menuItem2,new SeparatorMenuItem(),menuItem3);

        Menu menu2 = new Menu("Help");

        MenuItem menuItem4 = new MenuItem("Rules");
        menuItem4.setOnAction(e -> {
            showRules();
        });

        menu2.getItems().addAll(menuItem4);

        menuBar.getMenus().addAll(menu1,menu2);
        VBox root = new VBox(menuBar);
        Scene scene = new Scene(root, 680, 790);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        // Define the background
        Pane pane = new Pane();
        root.getChildren().add(pane);

        pane.setBackground(background);
        pane.getChildren().add(getBoard());

        highlight = false;
        pieces = new HashMap<Position, ImageView>();
        board = new Pane();
        pane.getChildren().add(board);

        String style1 = "-fx-font: 24px Ebrima; -fx-font-weight: bold;";
        String style2 = "-fx-font: 18px Ebrima;";

        // Load UI for Player 1
        double p1Y = originBoard.getY()+655.0;

        p1Process = new ImageView(loadImg(player1.isHuman()?"brain.png":"gear.png"));
        p1Process.setX(25.0);
        p1Process.setY(p1Y);
        p1Process.setOpacity(1.0);
        pane.getChildren().add(p1Process);

        Text name1 = new Text(player1.getName());
        name1.setStyle(style1);
        name1.setLayoutX(70.0);
        name1.setLayoutY(p1Y+22);
        pane.getChildren().add(name1);

        p1Nodes = new Text();
        p1Nodes.setStyle(style2);
        p1Nodes.setLayoutX(280.0);
        p1Nodes.setLayoutY(p1Y+22);
        pane.getChildren().add(p1Nodes);

        // Load UI for Player 2
        double p2Y = originBoard.getY()-45.0;

        p2Process = new ImageView(loadImg(player2.isHuman()?"brain.png":"gear.png"));
        p2Process.setX(25.0);
        p2Process.setY(p2Y);
        p2Process.setOpacity(0.0);
        pane.getChildren().add(p2Process);

        Text name2 = new Text(player2.getName());
        name2.setStyle(style1);
        name2.setLayoutX(70.0);
        name2.setLayoutY(p2Y+22);
        pane.getChildren().add(name2);

        p2Nodes = new Text();
        p2Nodes.setStyle(style2);
        p2Nodes.setLayoutX(280.0);
        p2Nodes.setLayoutY(p2Y+22);
        pane.getChildren().add(p2Nodes);

        drawBoard();
        checkAImove();
    }

    private static String getNodeString(Player player)
    {
        if(player.isHuman()||player.getExploredNodes()==0)
            return "";
        else
        if(player.getExploredNodes()==1)
            return "("+player.getExploredNodes()+" Node has been explored last move.)";
        else
            return "("+player.getExploredNodes()+" Nodes have been explored last move.)";
    }

    private static void updateNodes()
    {
        p1Nodes.setText(getNodeString(player1));
        p2Nodes.setText(getNodeString(player2));
    }

    private static void updateProcess(boolean on)
    {
        if(on)
        {
            if(gameState.getTurn()==1)
            {
                p1Process.setOpacity(1.0);
                p2Process.setOpacity(0.0);
            }
            else
            {
                p1Process.setOpacity(0.0);
                p2Process.setOpacity(1.0);
            }
        }
        else
        {
            p1Process.setOpacity(0.0);
            p2Process.setOpacity(0.0);
        }
    }

    private static void checkAImove()
    {
        int turn = gameState.getTurn();
        Player player = getPlayer(turn);
        Thread AI;
        if(player.isAI()&&player.isActive())
        {
            AI = new Thread(()-> {
                    long startTime = System.nanoTime();
                    Move move = player.getAImove(gameState.clone());
                    long endTime = System.nanoTime();
                    long timeElapsed = endTime - startTime;
                    float msElapsed = (float)(timeElapsed / 1000000);
                    Platform.runLater(() -> {
                        if(player.isActive())
                        {
                            Log.write(player.getName()
                                +" explored "+player.getExploredNodes()
                                +" node(s) in "+msElapsed/1000.0+"s");
                            AImove(move);
                            }
                    });
                });
            AI.start();
        }
    }

    private static void sleep(long time)
    {
        try{ Thread.sleep(time); }
        catch(InterruptedException e){ Error.send(false,e.toString()); }
    }

    public static void AImove(Move move)
    {
        if(gameState.isValidMove(move))
        {
            updateNodes();
            updateProcess(false);

            ImageView src = pieces.get(move.src);

            final ImageView cap;
            if(move.isCap())
                cap = pieces.get(move.getCapPos());
            else
                cap = null;

            if(src==null)
            {
                Error.send(false,"Can't find ImageView for src piece "+move.src.getString());
                move(move);
            }
            else if(move.isCap()&&cap==null)
            {
                Error.send(false,"Can't find ImageView for cap piece "+move.getCapPos().getString());
                move(move);
            }
            else
            {
                Thread path = new Thread(()-> {
                    int steps;
                    long sleepTime;
                    if(move.isCap())
                    {
                        steps = 120*(int)AImoveTime/1000;
                        sleepTime = 2*AImoveTime/steps;
                    }
                    else
                    {
                        steps = 60*(int)AImoveTime/1000;
                        sleepTime = AImoveTime/steps;
                    }
                    double ocpStep = 2.0/(double)steps;
                    double VecX = (80.0*(move.tar.X-move.src.X))/(double)steps;
                    double VecY = (80.0*(move.tar.Y-move.src.Y))/(double)steps;
                    for(int i=0;i<steps;i++)
                    {
                        final int step = i;
                        Platform.runLater(() -> {
                                src.setX(src.getX()+VecX);
                                src.setY(src.getY()+VecY);
                                if(move.isCap()&&step>=steps/2)
                                    cap.setOpacity(cap.getOpacity()-ocpStep);
                            });
                        sleep(sleepTime);
                    }
                    Platform.runLater(() -> {
                            move(move);
                        });
                });
                path.start();
            }
        }
        else
        {
            move(move);
        }
    }

    private static void HImove(Move move)
    {
        if(gameState.isValidMove(move))
            highlight = false;
        else
            highlight = true;
        move(move);
    }

    private static void HImove(Position src, Position tar)
    {
        HImove(new Move(src,tar));
    }

    private static void printState()
    {
        System.out.println("\n#~~~~~~~~~~~~~~~~~~~~~~~~~#");
        System.out.println("State:");    
        System.out.println(gameState.getBoardAsString());
        System.out.println("Player "+gameState.getTurn()+" - Possible Moves:");
        for(Move move : gameState.getAvailableMoves())
            System.out.println(move.getString());
    }

    private static void move(Move move)
    {
        int turn = gameState.getTurn();
        Player player = getPlayer(turn);

        if(gameState.isValidMove(move))
        {
            if(move.isCap())
                Log.write(player.getName()+" captures "+move.getString());
            else
                Log.write(player.getName()+" moves "+move.getString());
            gameState.move(move);
            if(!gameState.isOver())
            {
                printState();
                Log.write("Board evaluation: "+Analysis.evaluate(gameState));
            }
        }
        else
        {
            Log.write(player.getName()+" rejected move "+move.getString());
        }

        turn = gameState.getTurn();
        drawBoard();

        if(gameState.isOver())
            lostGame(turn);
        else
            checkAImove();
    }

    private static void drawBoard(Position selectedPiece)
    {
        if(!gameState.isOver())
            updateProcess(true);

        int turn = gameState.getTurn();
        Player player = getPlayer(turn);
        clearBoard();  

        // Draw the purple source position indications
        if(highlight)
        {
            // Draw the purple source position indications
            if(selectedPiece==null)
            {
                for(Position pos : gameState.getSrcPositionitions())
                {
                    Point2D coord = getCoord(pos);
                    ImageView iv = new ImageView(loadImg("source.png"));
                    iv.setX(coord.getX());
                    iv.setY(coord.getY());
                    board.getChildren().add(setTarget(iv));
                }
            }
            // Draw the green target position indications
            else
            {
                for(Position pos : gameState.getTarPositionitions(selectedPiece))
                {
                    board.getChildren().add(getTarget(pos,1.0));
                }
            }
        }

        // Draw the opponent's stones
        for(Position pos : gameState.getStones(State.otherPlayer(turn)))
        {
            ImageView iv = getPiece(pos);
            pieces.put(pos,iv);
            board.getChildren().add(iv);
        }

        // Draw the player's stones
        ArrayList<Position> positions = gameState.getStones(turn);
        if(player.isHuman())
        {
            board.getChildren().add(getTargets());
            if(highlight)
            {
                ArrayList<Position> src = gameState.getSrcPositionitions();
                for(Position pos : positions)
                {
                    ImageView iv;
                    if(src.contains(pos))
                        iv = getMovePiece(pos);
                    else
                        iv = getPiece(pos);
                    pieces.put(pos,iv);
                    board.getChildren().add(iv);
                }
            }
            else
            {
                for(Position pos : positions)
                {
                    ImageView iv = getMovePiece(pos);
                    pieces.put(pos,iv);
                    board.getChildren().add(iv);
                }
            }
        }
        else
        {
            for(Position pos : positions)
            {
                ImageView iv = getPiece(pos);
                pieces.put(pos,iv);
                board.getChildren().add(iv);
            }
        }
    }

    private static void drawBoard()
    {
        drawBoard(null);
    }

    private static String getIMGpath(int stone)
    {
        switch(stone)
        {
            case 1: return "blackPiece.png";
            case 2: return "whitePiece.png";
            case 3: return "blackKing.png";
            case 4: return "whiteKing.png";
            default: return null;
        }
    }

    private static ImageView getPiece(Position pos)
    {
        int player = gameState.getPlayer(pos);
        int stone = gameState.getStone(pos);
        ImageView iv = new ImageView(loadImg(getIMGpath(stone)));
        iv.setX(originBoard.getX()+pos.getX()*80.0);
        iv.setY(originBoard.getY()+pos.getY()*80.0);
        return setTarget(iv);
    }

    private static ImageView getMovePiece(Position pos)
    {
        ImageView iv = getPiece(pos);
        iv.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = iv.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putImage(iv.getImage());
            db.setContent(content);
            drawBoard(pos);
            updateProcess(false);
            event.consume();
        });
        iv.setOnDragDone(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                drawBoard();
                event.consume();
            }
        });
        return iv;
    }

    private static ImageView getBoard()
    {
        ImageView board = new ImageView(loadImg("board.jpg"));
        board.setX(originBoard.getX());
        board.setY(originBoard.getY());
        //return setTarget(board);
        return board;
    }

    /**
     * Set an object as reviever for a moved piece.
     */
    private static ImageView setTarget(ImageView tar)
    {
        tar.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });
        tar.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                ImageView src = (ImageView) event.getGestureSource();
                HImove(getPosition(src.getX(),src.getY()),getPosition(event.getX(),event.getY()));
                event.setDropCompleted(true);
                event.consume();
            }
        });
        return tar;
    }

    private static ImageView getTarget(Position pos, double opacity)
    {
        Point2D coord = getCoord(pos);
        ImageView iv = new ImageView(loadImg("target.png"));
        iv.setX(coord.getX());
        iv.setY(coord.getY());
        iv.setOpacity(opacity);
        return setTarget(iv);
    }

    /**
     * @return    invisible placeholders which accept drag-and-drop moves.
     */
    private static Pane getTargets()
    {
        Pane tar = new Pane();
        for(int x=0;x<8;x++)
            for(int y=0;y<8;y++)
                tar.getChildren().add(getTarget(new Position(x,y), 0.0));
        // if((x+y)%2==1)
        // tar.getChildren().add(getTarget(new Position(x,y), 0.0));
        return tar;
    }

    /**
     * @return    the board position at the given coordinates
     */
    private static Position getPosition(double valX, double valY)
    {
        int X = (int)((valX-originBoard.getX())/80.0);
        int Y = (int)((valY-originBoard.getY())/80.0);
        return new Position(X,Y);
    }

    /**
     * @return    the coordinates at the given board position
     */
    private static Point2D getCoord(Position pos)
    {
        double X = originBoard.getX()+pos.getX()*80.0;
        double Y = originBoard.getY()+pos.getY()*80.0;
        return new Point2D(X,Y);
    }

    private static void clearBoard()
    {
        board.getChildren().clear();
        pieces.clear();
    }
}
