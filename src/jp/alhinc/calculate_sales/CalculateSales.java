package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// 支店定義ファイル読み込み時の正規表現式
	private static String branchRegular = "^[0-9]{3}$";

	// 商品定義ファイル読み込み時の正規表現式
	private static String commodityRegular = "^[A-Za-z0-9]{8}$";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String NOT_EXIST = "が存在しません";
	private static final String INVALID_FORMAT = "のフォーマットが不正です";
	private static final String RCDFILES_NOT_CONTINOUS = "売上ファイル名が連番になっていません";
	private static final String BRANCH_INVALID_CORD = "の支店コードが不正です";
	private static final String COMMODITY_INVALID_CORD = "の商品コードが不正です";
	private static final String OF_BRANCH = "支店定義ファイル";
	private static final String OF_COMMODITY = "商品定義ファイル";
	private static final String SALEAMOUNT_OVER= "合計金額が10桁を超えました";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//*コマンドライン引数が渡されていない場合(エラー処理3)済
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales, OF_BRANCH, branchRegular)) {
			return;
		}

		// 商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		// 商品コードと売上金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();
		// 商品定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_COMMODITY_LST, commodityNames, commoditySales, OF_COMMODITY, commodityRegular)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)済
		//filesは売上集計課題フォルダの中身全部
		File[] files= new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();
		for(int i = 0; i < files.length; i++) {
			//filesの中身がファイルかつファイル名が「8桁.rcd」なら処理へ
			if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}.rcd$")) {
				//rcdFilesリストに、条件クリアしたもののみを追加
				//追加してるのはfiles[i]だから「8桁.rcd」の手前も書かれているパス
				rcdFiles.add(files[i]);
			}
		}

		//ソート追加 OS問わず使えるようにするため
		Collections.sort(rcdFiles);

		//*売上ファイルが連番になっていない場合(エラー処理2-1)済
		for(int i = 0; i < rcdFiles.size() -1; i++) {
			//rcdFiles.get(i)                         = args[0]\0000000@.rcd
			//rcdFiles.get(i).getName()               = 0000000@.rcd
			//rcdFiles.get(i).getName().substrin(0,8) = 0000000@
			int former = Integer.parseInt(((rcdFiles.get(i)).getName()).substring(0, 8));
			int latter = Integer.parseInt(((rcdFiles.get(i + 1)).getName()).substring(0, 8));
			if(latter - former != 1) {
				System.out.println(RCDFILES_NOT_CONTINOUS);
				return;
			}
		}

		//売上集計ファイル読み込み処理
		BufferedReader br = null;
		for(int i = 0; i < rcdFiles.size(); i++) {
			try {
				//rcdFiles.get(i) = args[0]\0000000@.rcd
				File file = rcdFiles.get(i);
				FileReader fr = new FileReader(file);
				//brはfileの場所
				br = new BufferedReader(fr);
				//「8桁.rcd」のファイルを1行ずつ読み込んでリストに追加する
				String line;
				List<String> filesales = new ArrayList<>();
				while((line = br.readLine()) != null) {
					//filesales = {支店コード, 商品コード,売上金額};になる
					filesales.add(line);
				}

				//*売上ファイルの中身が3行ではない場合(エラー処理2-4)済 エラーは大きい順に書く
				if(filesales.size() != 3) {
					//rcdFiles.get(i).getName() = 0000000@.rcd
					System.out.println(rcdFiles.get(i).getName() + INVALID_FORMAT);
					return;
				}

				//*売上ファイルの中に書いてある支店コードが支店定義ファイルに存在しない場合(エラー処理2-3)済
				if(!branchNames.containsKey(filesales.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + BRANCH_INVALID_CORD);
					return;
				}

				//*売上ファイルの中に書いてある商品コードが商品定義ファイルに存在しない場合(エラー処理2-3)済
				if(!commodityNames.containsKey(filesales.get(1))) {
					System.out.println(rcdFiles.get(i).getName() + COMMODITY_INVALID_CORD);
					return;
				}

				//*売上ファイルの中に書いてある売上金額が数字でない場合(エラー処理3)済
				if(!(filesales.get(2).matches("^[0-9]*$"))) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				//filesales = {支店コード, 商品コード,売上金額};
				//売上金額の型変更 String→Long
				long fileSale = Long.parseLong(filesales.get(2));

				//支店の合計金額
				long branchAmount = branchSales.get(filesales.get(0)) + fileSale;

				//商品の合計金額
				long commodityAmount = commoditySales.get(filesales.get(1)) + fileSale;

				//支店の合計金額が10桁を超えた場合(エラー処理2-2)済
				if(branchAmount >= 10000000000L || commodityAmount >= 10000000000L) {
					System.out.println(SALEAMOUNT_OVER);
					return;
				}

				//支店：計算後の売上金額をMapに書き換え
				branchSales.put(filesales.get(0), branchAmount);

				//商品：計算後の売上金額をMapに書き換え
				commoditySales.put(filesales.get(1), commodityAmount);
			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				//処理をストップするだけ。readFileは真偽返す必要あったからfalse書いてた。
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}
	//mainメソッドの}
	}

	/**
	 * 支店定義ファイルと商品定義ファイルの読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */

	//subject→対象
	private static boolean readFile(String path, String fileName, Map<String, String> cordNames, Map<String, Long> cordSales, String subject, String regular) {
		BufferedReader br = null;
		try {
			File file = new File(path, fileName);

			//*支店(商品)定義ファイルが存在しない場合(エラー処理1-1)済
			if(!file.exists()) {
				System.out.println(subject + NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			//brはfileの場所
			br = new BufferedReader(fr);
			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)済
				//String[] items = {支店コード,支店名} or {商品コード,商品名}
				String[] items = line.split(",");

				//*支店(商品)定義ファイルのフォーマットが不正な場合(エラー処理1-2)済
				if((items.length != 2) || (!items[0].matches(regular))) {
					//ofは引数でメソッドを呼び出した時の設定で変わる
					System.out.println(subject + INVALID_FORMAT);
					return false;
				}
				//支店定義ファイル読み込み→keyは支店コード、valueは支店名
				//商品定義ファイル読み込み→keyは商品コード、valueは商品名
				cordNames.put(items[0], items[1]);

				//支店定義ファイル読み込み→keyは支店コード、valueは0。
				//商品定義ファイル読み込み→keyは商品コード、valueは0。
				//「(long)0」は「0L」でも可。
				cordSales.put(items[0], (long)0);
			}
		} catch(IOException e) {
			System.out.println(subject + NOT_EXIST);
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
	//readFileメソッドの}
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
	private static boolean writeFile(String path, String fileName, Map<String, String> cordNames, Map<String, Long> cordSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)済
		BufferedWriter bw = null;
		try {
			//fileNameなくてもwriteで勝手に作ってくれるらしい。
			File file = new File(path,fileName);
			FileWriter fr = new FileWriter(file);
			//bwはfileの場所
			bw = new BufferedWriter(fr);

			for(String key: cordNames.keySet()) {
				//支店→keyは1個目のマップのkeyだから支店コード
				//商品→keyは1個目のマップのkeyだから商品コード
				//→これ全部取得するまで繰り返し
				//→001～作ったの最後まで
				bw.write(key + "," + cordNames.get(key) + "," + cordSales.get(key));
				//改行
				bw.newLine();
			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			//booleanだから真偽の返しほしい。
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
}