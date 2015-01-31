import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gennadiy on 31.01.2015.
 */
public class PaddingOracle {

    private String urlForRequests = "http://crypto-class.appspot.com/po?er=%s";



    public String getPlainText(String cipherText) {
        List<String> listOfBlocks = getListFromString(cipherText, 32);
        List<String> plaintTextList = new ArrayList<String> ();

        for (int i = listOfBlocks.size() - 2; i >=0; i--) {
            plaintTextList.add(decryptBlock(listOfBlocks.get(i), listOfBlocks.get(i + 1)));

        }

        StringBuilder finalString = new StringBuilder();
        for (int i = plaintTextList.size() - 1; i >=0; i--) {
            finalString.append(plaintTextList.get(i));

        }

        return finalString.toString();

    }


    private static List<String> getListFromString(String incomeString, int splitFactor) {
        return Lists.newArrayList(Splitter.fixedLength(splitFactor).split(incomeString));
    }

    private String decryptBlock(String blockToFake, String blockToDecrypt){

        byte padSize = 1;
        byte[] guessedBytes = new byte[blockToFake.length() / 2];
        for (int i = 0; i < guessedBytes.length; i++) {

            for (byte guessByte = 2; guessByte < 123; guessByte++) {


                try {
                    URL requestUrl = new URL(String.format(urlForRequests, getFakeBlockString(getBytesFromString(blockToFake), guessByte, guessedBytes, padSize) + blockToDecrypt));

                    HttpURLConnection requestConnection = (HttpURLConnection) requestUrl.openConnection();
                    if (requestConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                        guessedBytes[padSize - 1] = guessByte;
                        padSize++;
                        break;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        StringBuilder blockText = new StringBuilder();
        for (int i = 0; i < guessedBytes.length; i++) {
            blockText.append(Character.toChars(guessedBytes[i]));
        }


        return blockText.reverse().toString();
    }

    private String getFakeBlockString(byte[] blockToFake, byte guessByte, byte[] guessedBytes, byte padSize) {
        for (int i = 1; i < padSize + 1; i++) {
            if (i == padSize) {
                blockToFake[blockToFake.length - i] = (byte)(blockToFake[blockToFake.length - i] ^ guessByte ^ padSize);
            } else {
                blockToFake[blockToFake.length - i] = (byte)(blockToFake[blockToFake.length - i] ^ guessedBytes[i - 1] ^ padSize);
            }
        }

        return getHexStringOfBytes(blockToFake);


    }



    private byte[] getBytesFromString(String blockString) {
        byte[] resultByteArray = new byte[blockString.length() / 2];
        List<String> byteList = getListFromString(blockString,2);
        for (int i = 0; i < byteList.size(); i++) {
            resultByteArray[i] = (byte)Integer.parseInt(byteList.get(i), 16);
        }

        return resultByteArray;
    }

    private String getHexStringOfBytes(byte[] hashToString) {

        StringBuilder hashBuilder = new StringBuilder();

        for (byte b : hashToString) {
            hashBuilder.append(String.format("%02x", b));
        }

        return hashBuilder.toString();

    }


}
