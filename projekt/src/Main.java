import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        new ChessGame();
    }
}
class ChessGame {


    private ChessBoard chessBoard;
    private boolean isBlackTurn;
    private final String menu = """
            1. x - zakończ grę
            2. o - zakończ grę remisem
            3. s - zapisz grę
            4. h - wyświetl menu
            """;

    private void gameBeginning(){
        Scanner scan = new Scanner(System.in);
        char scannedChar;
        System.out.println(menu);
        System.out.println("Naciśnij u aby rozpocząć rozgrywkę lub l aby wczytać grę");
        scannedChar = scan.next().charAt(0);

        if (scannedChar == 'u') {
            chessBoard = new ChessBoard();
            isBlackTurn = chessBoard.isTurn();
        } else if (scannedChar == 'l') {
            chessBoard = loadGame();
            isBlackTurn = chessBoard.isTurn();
        }
        else{
            gameBeginning();
        }
    }

    private boolean middleGame(){
        Scanner scan = new Scanner(System.in);
        String moveString;
        int[] move = new int[2];
        int[] cords = new int[2];

        String whoseTurn = !isBlackTurn ? "Białych" : "Czarnych";
        System.out.println("Teraz tura: " + whoseTurn);

        if (isBlackTurn) {
            chessBoard.printBoard();
        } else {
            chessBoard.printBoardReversed();
        }


        System.out.println("Podaj swój ruch: (np. a2a4).");
        moveString = scan.nextLine().trim();

        if(moveString.length() != 4) {
            switch (moveString) {
                case "x" -> {
                    System.out.println("Dziekuję za gre :D");
                    return false;
                }
                case "s" -> {
                    saveGame(chessBoard);
                    System.out.println("Gra zapisana");
                    return false;
                }
                case "o" -> {
                    System.out.println("Czy drugi gracz również chce zakończyć grę remisem ?");
                    moveString = scan.nextLine().trim();
                    if (moveString.equals("o")) {
                        System.out.println("Gra zakończona remisem.");
                        return false;
                    } else {
                        middleGame();
                    }
                }
                case "h" -> {
                    System.out.println(menu);
                    middleGame();
                }
                default -> {
                    System.out.println("Niepoprawny ruch !");
                    middleGame();
                }
            }
        }

        cords[0] = Character.toLowerCase(moveString.charAt(0)) - 97;
        cords[1] = moveString.charAt(1) - 49;

        move[0] = Character.toLowerCase(moveString.charAt(2)) - 97;
        move[1] = moveString.charAt(3) - 49;

        Elements elem = chessBoard.getBoard()[cords[1]][cords[0]];

        if(elem == null){
            System.out.println("Na tym polu nie ma żadnej figury");
            middleGame();
        }
        else if(!chessBoard.move(elem, move[0], move[1], isBlackTurn)){
            System.out.println("Nie prawidłowy ruch");
            middleGame();
        }
        isBlackTurn = !isBlackTurn;
        middleGame();
        return true;
    }

    public ChessGame() {

        gameBeginning();
        middleGame();

        }



    public void saveGame(ChessBoard currentBoard) {
        ArrayList<ArrayList<Object>> saveBoard = currentBoard.getPlayingBoard();
        ArrayList<Object> deepestList = (ArrayList<Object>) saveBoard.get(0).get(0);
        ArrayList<Elements> singularSaveBoard = new ArrayList<>();


        for(int i = 1; i < saveBoard.size();i++){
            for(int j = 1; j < saveBoard.get(1).size();j++){
                singularSaveBoard.add((Elements) saveBoard.get(i).get(j));
            }
        }

        for(Object ob : deepestList){
            singularSaveBoard.add((Elements) ob);
        }

        for (Elements elem:
             singularSaveBoard) {
            if(elem != null) {
                System.out.println("element " + elem.getPositionX() + " " + elem.getPositionY() + " " + elem.getType() + " " + elem.isBlack());
            }
        }


        String savingFolderPath = "./saves";
        File folder = new File(savingFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        Scanner scan = new Scanner(System.in);
        System.out.println("Proszę podaj nazwę pliku do zapisu.");
        String usersPath = scan.nextLine();
        try {
            FileInputStream fis = new FileInputStream(savingFolderPath + "/" + usersPath + ".bin");
            fis.close();
        } catch (FileNotFoundException x) {
            try {
                FileOutputStream fos = new FileOutputStream(savingFolderPath + "/" + usersPath + ".bin");
                for(Elements element:singularSaveBoard){
                        if (element != null) {
                            byte color = (byte) (element.isBlack() ? 1 : 0);
                            int save = (color << 11) | ((element.getPositionY() + 1) << 7) | ((element.getPositionX() + 1) << 3) | element.getType().getNumberSignature();
                            System.out.println(save);

                            byte[] bytes = new byte[2];
                            bytes[1] = (byte) (save & 0xFF);
                            bytes[0] = (byte) ((save >> 8) & 0xFF);
                            fos.write(bytes);
                        }
                    }
                fos.close();
            } catch (IOException l) {
                System.out.println("Wystąpił problem z zapisem pliku");
            }
        } catch (IOException x) {
            System.out.println("Problem z zapisem");
        }
    }

    public ChessBoard loadGame(){
        String savePath = "./saves";

        ChessBoard loadedChessboard = new ChessBoard(true);

        int counter = 1;
        File folder = new File(savePath);
        File[] files = null;
        if(folder.exists() && folder.isDirectory()){
            files = folder.listFiles();
            if(files != null){
                for(File file: files){
                    System.out.println(counter++ + " " + file.getName());
                }
            }
        }

        Scanner scan = new Scanner(System.in);
        System.out.println("Wybierz numer pliku");
        boolean isCorrectFile = false;
        int input = 0;
        while(!isCorrectFile){
            input = scan.nextInt();
            isCorrectFile = input >= 1 && input <= files.length;
        }

        try{
            FileInputStream fis = new FileInputStream(savePath+"/"+files[input - 1].getName());
            int gameRead;
            while((gameRead = fis.read()) != -1){
                byte[] bytes = new byte[2];
                bytes[0] = (byte)gameRead;
                bytes[1] = (byte)fis.read();

                int figureType = bytes[1] & 0b00000111;
                int posX =  ((bytes[1] & 0b01111000) >> 3);
                int posY = (((bytes[0] & 0b00000111) << 1) | ( (bytes[1] & 0b10000000) >> 7));
                boolean color = ((bytes[0] & 0b00001000) >> 3) == 1;


                loadedChessboard.placeTheFigure(posX, posY , figureType, color);


            }
            fis.close();
        }
        catch(FileNotFoundException e){
            System.out.println("Nie ma takiego pliku");
        }
        catch(IOException x){
            System.out.println("Wystąpił problem");
        }
        return loadedChessboard;
    }
}

class ChessBoard {
    private final static int SIZE_X = 8;
    private final static int SIZE_Y = 8;
    private ArrayList<ArrayList<Object>> playingBoard = new ArrayList<>();
    private Elements[][] board = new Elements[SIZE_Y][SIZE_X];

    private boolean turn;

    //arraylist do zapisywania elementu oraz jego poprzedniej destynacji
    private ArrayList<Object> lastMovedPawn = new ArrayList<>();
    private ArrayList<Elements> killedFigures = new ArrayList<>();

    public ChessBoard(){
        boardInit();
    }

    public ChessBoard(boolean loading){
        initializeArrayList();
    }

    private void boardInit(){
        //biale piony
        for(int i = 0; i < board[1].length; i++){
            board[1][i] = new Pawn(i,1,false);
        }

        //czarne piony
        for(int i = 0; i < board[6].length; i++){
            board[6][i] = new Pawn(i,6,true);
        }

        //biale reszta
        board[0][0] = new Rook(0, 0, false);
        board[0][1] = new Knight(1, 0, false);
        board[0][2] = new Bishop(2, 0, false);
        board[0][3] = new Queen(3, 0, false);
        board[0][4] = new King(4, 0, false);
        board[0][5] = new Bishop(5, 0, false);
        board[0][6] = new Knight(6, 0, false);
        board[0][7] = new Rook(7, 0, false);
        board[7][0] = new Rook(0, 7, true);
        board[7][1] = new Knight(1, 7, true);
        board[7][2] = new Bishop(2, 7, true);
        board[7][3] = new Queen(3, 7, true);
        board[7][4] = new King(4, 7, true);
        board[7][5] = new Bishop(5, 7, true);
        board[7][6] = new Knight(6, 7, true);
        board[7][7] = new Rook(7,7, true);

        this.turn = false;

        initializeArrayList();

    }

    private void initializeArrayList(){
        for (int i = 0; i < 9; i++) {
            ArrayList<Object> arr1 = new ArrayList<>();
            for (int j = 0; j < 9; j++) {
                arr1.add(null);
            }
            playingBoard.add(arr1);
        }
    }

    private void fillPlayingArrayList(){
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++) {
                if(i == 0 && j == 0){
                    playingBoard.get(i).set(j, killedFigures);
                }
                else if(i > 0 && j > 0) {
                    playingBoard.get(i).set(j, board[i - 1][j - 1]);
                }
            }
        }
    }

    @FunctionalInterface
    interface BordersChecker{
        boolean borders(int x, int y);
    }

    @FunctionalInterface
    interface IterateOnPath{
        boolean check(int destiX, int destiY);
    }
    public void placeTheFigure(int boardPosX, int boardPosY, int figureType, boolean isBlack){
        boardPosX-=1;
        boardPosY-=1;
        Elements[] allFigures = {new Pawn(boardPosX, boardPosY, isBlack), new King(boardPosX, boardPosY, isBlack), new Queen(boardPosX, boardPosY, isBlack), new Rook(boardPosX, boardPosY, isBlack), new Bishop(boardPosX, boardPosY, isBlack), new Knight(boardPosX, boardPosY, isBlack)};
        if(boardPosX != -1 && boardPosY != -1) {
            board[boardPosY][boardPosX] = allFigures[figureType];
        }
        else {
            killedFigures.add(allFigures[figureType]);
        }
    }

    private boolean checkCastling(Elements elem, int destX, int destY){
        if(elem != null && board[destY][destX] != null) {
            if (elem.getType() == FigureType.KING) {
                King king = (King) elem;
                if (board[destY][destX].getType() == FigureType.ROOK) {
                    Rook rook = (Rook) board[destY][destX];
                    return !king.hasMoved() && !rook.hasMoved() && (Math.abs(destX - elem.getPositionX()) == 4 || Math.abs(destX - elem.getPositionX()) == 3);
                }
            } else if (elem.getType() == FigureType.ROOK) {
                Rook rook = (Rook) elem;
                if (board[destY][destX].getType() == FigureType.KING) {
                    King king = (King) board[destY][destX];
                    return !king.hasMoved() && !rook.hasMoved() && (Math.abs(destX - elem.getPositionX()) == 4 || Math.abs(destX - elem.getPositionX()) == 3);
                }
            }
        }
        return false;
    }

    //sprawdzanie ruchu
    private boolean checkMove(Elements elem, int destX, int destY, boolean isBlackTurn, boolean isChecking){
        int posX = elem.getPositionX();
        int posY = elem.getPositionY();


        BordersChecker borders = (x, y) -> x >= 0 && x < SIZE_X && y >= 0 && y < SIZE_Y;

        //sprawdzamy czy na drodze figury nic nie stoi
        IterateOnPath it = (destiX, destiY) -> {
            int compareX = Integer.compare(destiX, posX);
            int compareY = Integer.compare(destiY, posY);
            int stepX = posX + compareX;
            int stepY = posY + compareY;
            while(stepX != destiX || stepY != destiY){
                if(board[stepY][stepX] != null){
                    return false;
                }
                stepX += compareX;
                stepY += compareY;
            }
            return true;
        };


        if (elem.isBlack() == isBlackTurn) {
            if(borders.borders(destX, destY)) {
                if (elem.possibleMove(destX, destY) || (elem.getType() == FigureType.PAWN && checkCapture(elem,destX,destY)) || checkCastling(elem, destX, destY)) {
                    if(elem.getType() == FigureType.KNIGHT || it.check(destX, destY)) {
                        return board[destY][destX] == null || checkCapture(elem, destX, destY) || (isChecking && board[destY][destX].getType() == FigureType.KING) || checkCastling(elem, destX, destY);
                    }
                    else {
                        return false;
                        }
                    }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        else{
        return false;
        }
    }

    private ArrayList<int[]> getValidMoves(Elements elem) {
        ArrayList<int[]> validMoves = new ArrayList<>();

        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (checkMove(elem, x, y, elem.isBlack(), false)) {
                    int[] move = {x,y};
                    validMoves.add(move);
                }
            }
        }

        return validMoves;
    }

    //sprawdzamy enPassant
    private boolean checkEnPassant(Elements elem, int destX, int destY){
        if(lastMovedPawn.size() == 4) {
            Pawn before = (Pawn) lastMovedPawn.get(0);
            int beforeDestX = before.getPositionX();
            int beforeDestY = before.getPositionY();
            boolean hasMoved = (boolean)lastMovedPawn.get(3);
            if (before != null) {
                return beforeDestX == destX && !hasMoved && ((destY - beforeDestY == 1 && before.isBlack() != elem.isBlack() && !elem.isBlack()) || (destY - beforeDestY == -1 && before.isBlack() != elem.isBlack() && elem.isBlack()));
            }
        }
        return false;
    }

    public boolean move(Elements elem, int destX, int destY, boolean isBlackTurn) {
        int posX =  elem.getPositionX();
        int posY = elem.getPositionY();


        if(checkCastling(elem, destX, destY)){
            board[destY][destX].setPosition(posX, posY);
            board[posY][posX].setPosition(posX,posY);
            board[posY][posX] = board[destY][destX];
            board[destY][destX] = elem;
            return true;
        }


        if(checkEnPassant(elem, destX, destY)){
            Pawn pawn = (Pawn)lastMovedPawn.get(0);
            killedFigures.add(board[pawn.getPositionY()][pawn.getPositionX()]);
            board[pawn.getPositionY()][pawn.getPositionX()] = null;
            elem.setPosition(destX, destY);
            board[posY][posX] = null;
            board[destY][destX] = elem;
            return true;
        }

        if(elem.getType() == FigureType.PAWN){
            Pawn pawn = (Pawn) elem;
            if(lastMovedPawn.size() == 0) {
                lastMovedPawn.add(pawn);
                lastMovedPawn.add(pawn.getPositionX());
                lastMovedPawn.add(pawn.getPositionY());
                lastMovedPawn.add(pawn.hasMoved());
            }
            else {
                lastMovedPawn.set(0,pawn);
                lastMovedPawn.set(1, pawn.getPositionX());
                lastMovedPawn.set(2, pawn.getPositionY());
                lastMovedPawn.set(3, pawn.hasMoved());
            }
        }


        if(checkMove(elem, destX, destY, isBlackTurn, false)) {
            if(board[destY][destX] != null){
                killedFigures.add(board[destY][destX]);
                board[destY][destX] = null;
            }
            elem.setPosition(destX, destY);
            board[destY][destX] = elem;
            board[posY][posX] = null;
            //jezeli pionek to wtedy zablokuj nastepny podwojny ruch i sprawdz czy moze sie zamienic
            if (elem.getType() == FigureType.PAWN) {
                ((Pawn) elem).setFirstMoveMade();
                if((destY == 7 && !elem.isBlack()) || (destY == 0 && elem.isBlack())){
                    board[destY][destX] = new Queen(destX,destY, elem.isBlack());
                }
            }


            if (this.isCheck(isBlackTurn)) {
                System.out.println("Szach");
                if (this.isCheckMate(isBlackTurn)) {
                    System.out.println("szach mat :D");
                    throw new RuntimeException("Koniec Gry! Wygrały figury " + (isBlackTurn ? "czarne" : "białe"));
                }
            }
            fillPlayingArrayList();
            return true;
        }
        return false;
    }

    //sprawdzamy możliwości bicia
    private boolean checkCapture(Elements elem,int destX,int destY){
        if(elem.getType() == FigureType.PAWN && board[destY][destX] != null){
            int posX = elem.getPositionX();
            int posY = elem.getPositionY();
            int absolute = Math.abs(posX - destX);
            return (absolute == 1 && posY - destY == -1 && !elem.isBlack() && elem.isBlack() != board[destY][destX].isBlack()) ||
                    (absolute == 1 && posY - destY == 1 && elem.isBlack() && elem.isBlack() != board[destY][destX].isBlack());
        }
        else if(checkEnPassant(elem, destX, destY)){
            System.out.println("jest enPassnt");
            return true;
        }
        else return elem.getType() != FigureType.PAWN && board[destY][destX] != null && board[destY][destX].getType() != FigureType.KING && (board[destY][destX].isBlack() != elem.isBlack());
    }

    //znajdujemy króla
    private int[] findKing(boolean isBlackTurn) {
        Elements[][] board = this.getBoard();
        int[] kingPos = new int[2];

        IterateOnPath kingFinder = (destiX, destiY) -> {
            Elements element = board[destiY][destiX];
            return element != null && element.getType() == FigureType.KING && element.isBlack() == isBlackTurn;
        };

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (kingFinder.check(j, i)) {
                    kingPos[0] = j;
                    kingPos[1] = i;
                    return kingPos;
                }
            }
        }

        return kingPos;
    }

    //sprawdzamy czy szach jeżeli jakaś figura ma możliwy ruch na króla przeciwnika
    private boolean isCheck(boolean isBlackTurn){
        int[] kingPos = findKing(!isBlackTurn);
        int kingX = kingPos[0];
        int kingY = kingPos[1];


        for (Elements[] elements : board) {
            for (Elements elem : elements) {
                if (elem != null && elem.isBlack() == isBlackTurn) {
                    if (this.checkMove(elem, kingX, kingY, isBlackTurn, true)) {
                        System.out.println("Szach !!! Na elemencie: " + elem.getPositionX() + " " + elem.getPositionY());
                        return true;
                    }
                }
            }
        }
            return false;
        }

    //symulujemy plansze i sprawdzamy czy jakiś ruch umożliwi wyjście z szacha
    public boolean isCheckMate(boolean isBlackTurn) {

        Elements[][] board = this.getBoard();
        for (Elements[] elements : board) {
            for (int j = 0; j < elements.length; j++) {
                Elements figure = elements[j];
                if (figure != null && figure.isBlack() != isBlackTurn) {
                    ArrayList<int[]> moves = getValidMoves(figure);
                    for (int[] move : moves) {
                        ChessBoard simulatedBoard = new ChessBoard(true);
                        simulatedBoard.setBoard(board);
                        simulatedBoard.move(figure, move[0], move[1], !isBlackTurn);
                        if (!simulatedBoard.isCheck(isBlackTurn)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }



        public void printBoard() {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    String color = (i + j) % 2 == 0 ? "\u001B[47m" : "\u001B[40m";
                    System.out.print(color);
                    if (board[i][j] != null) {
                        color = "\u001B[" + (board[i][j].isBlack() ? "31m" : "32m");
                        System.out.print(color + board[i][j].signature());
                    } else {
                        System.out.print((i+j)%2==0 ? "\u001B[38m\u2659" : "\u001B[37m\u2659");
                    }
                    System.out.print("\u001B[49m");
                }
                System.out.println();
            }
            System.out.print("\u001B[0m");
            System.out.println("A B C D E F G H");
        }



        public void printBoardReversed(){
            for (int i = board.length - 1; i >= 0; i--) {
                for (int j = 0; j < board[i].length; j++) {
                    String color = (i + j) % 2 == 0 ? "\u001B[47m" : "\u001B[40m";
                    System.out.print(color);
                    if (board[i][j] != null) {
                        color = "\u001B[" + (board[i][j].isBlack() ? "31m" : "32m");
                        System.out.print(color + board[i][j].signature());
                    } else {
                        System.out.print((i+j)%2==0 ? "\u001B[38m\u2659" : "\u001B[37m\u2659");
                    }
                    System.out.print("\u001B[49m");
                }
                System.out.println();
            }
            System.out.print("\u001B[0m");
            System.out.println("A B C D E F G H");
        }
    public Elements[][] getBoard() {
        return board;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setBoard(Elements[][] elems){
      for(int i = 0; i < board.length; i++){
          for(int j = 0; j < board[i].length; j++){
              board[i][j] = elems[i][j];
          }
      }
    }

    public ArrayList<ArrayList<Object>> getPlayingBoard() {
        return playingBoard;
    }
}




interface Killable {

}

interface Verifiable {
    boolean hasMoved();
    void setFirstMoveMade();
}



enum FigureType{
    PAWN((short)0),
    ROOK((short)3),
    KNIGHT((short)5),
    BISHOP((short)4),
    QUEEN((short)2),
    KING((short)1);

    private final short numberSignature;

    FigureType(short numberSignature){
        this.numberSignature = numberSignature;
    }

    public int getNumberSignature(){
        return numberSignature;
    }
        }
abstract class Elements {
    private int positionX;
    private int positionY;
    private FigureType type;
    private boolean isBlack;

    public Elements(int positionX, int positionY, boolean isBlack, FigureType type){
        this.positionX = positionX;
        this.positionY = positionY;
        this.isBlack = isBlack;
        this.type = type;
    }

    public abstract boolean possibleMove(int newPositionX, int newPositionY);
    public abstract char signature();

    public void setPosition(int positionX, int positionY){
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public FigureType getType() {
        return type;
    }
    public boolean isBlack() {
        return isBlack;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }
}


class Pawn extends Elements implements Killable, Verifiable {
    private boolean firstMoveMade;

    public Pawn(int positionX, int positionY, boolean isBlack) {
        super(positionX, positionY, isBlack, FigureType.PAWN);
        this.firstMoveMade = false;
    }

    @Override
    public boolean possibleMove(int newPositionX, int newPositionY) {

        int currentX = this.getPositionX();
        int currentY = this.getPositionY();

        return (newPositionX == currentX && newPositionY == currentY + 1 && !isBlack()) ||
                (newPositionX == currentX && newPositionY == currentY - 1 && isBlack()) ||
                ((newPositionX == currentX && newPositionY == currentY + 2) && !firstMoveMade && !isBlack()) ||
                ((newPositionX == currentX && newPositionY == currentY - 2) && !firstMoveMade && isBlack());
    }



    @Override
    public char signature() {
        return '♟';
    }


    @Override
    public boolean hasMoved() {
        return firstMoveMade;
    }

    public void setFirstMoveMade() {
        this.firstMoveMade = true;
    }

}

class Knight extends Elements implements Killable {

    public Knight(int positionX, int positionY, boolean isBlack) {
        super(positionX, positionY, isBlack, FigureType.KNIGHT);
    }

    @Override
    public boolean possibleMove(int newPositionX, int newPositionY) {
        int currentX = this.getPositionX();
        int currentY = this.getPositionY();

        return ((Math.abs(newPositionX - currentX) == 1) &&
                (Math.abs(newPositionY - currentY) == 2)) ||
                ((Math.abs(newPositionX - currentX) == 2) &&
                        (Math.abs(newPositionY - currentY) == 1));
    }

    @Override
    public char signature() {
        return '♞';
    }


}
class Bishop extends Elements implements Killable {

    public Bishop(int positionX, int positionY, boolean isBlack) {
        super(positionX, positionY, isBlack, FigureType.BISHOP);
    }

    @Override
    public boolean possibleMove(int newPositionX, int newPositionY) {
        int currentX = this.getPositionX();
        int currentY = this.getPositionY();
        // Math.abs do policzenia wartości bezwzględnej

        return Math.abs(newPositionX - currentX) == Math.abs(newPositionY - currentY);
    }

    @Override
    public char signature() {
        return '♝';
    }


}
class Rook extends Elements implements Killable, Verifiable {
    private boolean firstMoveMade;

    public Rook(int positionX, int positionY, boolean isBlack) {
        super(positionX, positionY, isBlack, FigureType.ROOK);
        this.firstMoveMade = false;
    }

    @Override
    public boolean possibleMove(int newPositionX, int newPositionY) {

        int currentX = this.getPositionX();
        int currentY = this.getPositionY();

        return ((currentX == newPositionX) || (currentY == newPositionY));
    }

    @Override
    public char signature() {
        return '♜';
    }


    @Override
    public boolean hasMoved() {
        return firstMoveMade;
    }

    public void setFirstMoveMade() {
        firstMoveMade = true;
    }

}

class Queen extends Elements implements Killable{
    public Queen(int positionX, int positionY, boolean isBlack) {
        super(positionX, positionY, isBlack, FigureType.QUEEN);
    }

    @Override
    public boolean possibleMove(int newPositionX, int newPositionY) {
        int currentX = this.getPositionX();
        int currentY = this.getPositionY();

        return (Math.abs(newPositionX - currentX) == Math.abs(newPositionY - currentY)) ||
        ((currentX == newPositionX) || (currentY == newPositionY));
    }

    @Override
    public char signature() {
        return '♛';
    }


}

class King extends Elements implements Verifiable {
    private boolean firstMoveMade;

    public King(int positionX, int positionY, boolean isBlack) {
        super(positionX, positionY, isBlack, FigureType.KING);
        this.firstMoveMade = false;
    }

    @Override
    public boolean possibleMove(int newPositionX, int newPositionY) {
        int currentX = this.getPositionX();
        int currentY = this.getPositionY();


        return (Math.abs(newPositionX - currentX) <= 1 && Math.abs(newPositionY - currentY) <= 1);
    }

    @Override
    public boolean hasMoved() {
        return firstMoveMade;
    }

    public void setFirstMoveMade() {
        this.firstMoveMade = true;
    }

    @Override
    public char signature() {
        return '♚';
    }
}



