package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)済
		File[] files= new File(args[0]).listFiles(); //filesは売上集計課題フォルダの中全部
		List<File> rcdFiles = new ArrayList<>();

		for(int i = 0;i < files.length;i++) {
			if(files[i].getName().matches("^[0-9]{8}.rcd$")) { //files[i].getName();ファイル名部分のみもらい
				rcdFiles.add(files[i]);
				//rcdFilesリストに、ファイル名「8桁.rcd」の条件合致したのを追加
				//追加してるのはfiles[i]だから「8桁.rcd」の手前も書かれてるパス
			}
		}

		//売上集計ファイル読み込み処理
		BufferedReader sa = null; //sale

		for(int i = 0;i < rcdFiles.size();i++) {
			try {
				File rfile = rcdFiles.get(i);
				FileReader rfr = new FileReader(rfile);
				sa = new BufferedReader(rfr); //saはfileの場所

				String rline;
				List<String> filesales = new ArrayList<>(); //リストfilesales
				while((rline = sa.readLine()) != null) {
					filesales.add(rline); //「8桁.rcd」ファイルを1行ずつ読み込んでリストに追加
					}

				//売上金額の型変更 String→Long
				long fileSale = Long.parseLong(filesales.get(1));

				//売上金額を足す
				long saleAmount = branchSales.get(filesales.get(0)) + fileSale;

				//計算後の売上金額をMapに追加（書き換え）
				branchSales.put(filesales.get(0),saleAmount);
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return; //処理をストップするだけ。readFileは真偽返す必要あったからfalse書いてた。
				} finally {
					// ファイルを開いている場合
					if(sa != null) {
						try {
							// ファイルを閉じる
							sa.close();
						} catch(IOException e) {
							System.out.println(UNKNOWN_ERROR);
							return;
						}
					}
				}
			} //forの}


		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
	} //mainクラスの}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr); //brはfileの場所

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)済
				String[] items = line.split(","); //String[] items = {支店コード,支店名};
						branchNames.put(items[0],items[1]); //keyは支店コード、valueは支店名
						branchSales.put(items[0],(long)0); //keyは支店コード、valueは0
			}
		} catch(IOException e) {
			System.out.println(FILE_NOT_EXIST);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 * 支店別集計ファイル名
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)済
		BufferedWriter bo = null;

			try {
				File bfile = new File(path,fileName); //fileNameなくてもwriteで勝手に作ってくれるらしい。便利！
				FileWriter bfr = new FileWriter(bfile);
				bo = new BufferedWriter(bfr); //boはfileの場所

				for(String key:branchNames.keySet()) {
//					keyはbranchNamesのkeyだから支店コード
//					→これ全部取得するまで繰り返し
//					→001～作ったの最後まで
					bo.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
					bo.newLine(); //改行
					}
				}catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false; //booleanだから真偽の返しほしい。
				} finally {
					// ファイルを開いている場合
					if(bo != null) {
						try {
							// ファイルを閉じる
							bo.close();
						} catch(IOException e) {
							System.out.println(UNKNOWN_ERROR);
							return false;
						}
					}
				}
				return true;
	}

}
