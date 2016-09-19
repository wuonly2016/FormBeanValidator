/**
 * FormBeanValidater
 * common validate 
 * support request and Javabean
 * support validations：
 * notNull 
 * isNum 
 * isEmail mail address
 * maxLen max length of a string
 * minLen  min length of a string
 * maxInt max value of a integer
 * minInt min value of a integer
 * isIdNum chinese ID number
 ... to be added
 * 
 */
package com.***.validater;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.codyy.smp.service.mybatis.entity.SmpDemo;
import com.codyy.smp.service.util.RequestUtils;

public class FormBeanValidater<T>
{

	protected Class<? extends Object> clazz;
	public Log log = LogFactory.getLog( this.getClass( ) );

	public boolean validateFormObject( T t, HttpServletRequest request )
	{
		Map<String, List<String>> reMap = new HashMap<String, List<String>>( );
		boolean retVal = true;
		try
		{
			clazz = t.getClass( );
			Field[] fields = clazz.getDeclaredFields( );
			for ( Field f : fields )
			{
				if ( f.isAnnotationPresent( FormBeanValidate.class ) )
				{
					FormBeanValidate fv = f
							.getAnnotation( FormBeanValidate.class );
					String fvalue = RequestUtils.getRquestParam( request,
							f.getName( ) );
					String[] fvMarchers = fv.marcher( );
					if ( fvMarchers != null && fvMarchers.length > 0 )
					{
						List<String> msgList = new ArrayList<String>( );
						for ( String fvMarcher : fvMarchers )
						{
							if ( fvMarcher.equals( "notNull" ) )
							{
								if ( StringUtils.isBlank( fvalue ) )
								{// 判断空
									msgList.add( "不能为空" );
								}
							}
							if ( StringUtils.isBlank( fvalue ) )
							{// 空值不做判断
								continue;
							}
							if ( fvMarcher.equals( "isIdNum" ) )
							{
								if ( !isIdNum( fvalue ) )
								{
									log.error( f.getName( ) + ":不是有效身份证号码" );
									msgList.add( "不是有效身份证号码" );
								}
							}
							else if ( fvMarcher.equals( "isEmail" ) )
							{
								if ( !isEmail( fvalue ) )
								{
									log.error( f.getName( ) + ":不是有效邮箱" );
									msgList.add( "不是有效邮箱" );
								}
							}
							else if ( fvMarcher.equals( "isNum" ) )
							{
								if ( !StringUtils.isNumeric( fvalue ) )
								{
									log.error( f.getName( ) + ":不是数字" );
								}
							}
							else if ( fvMarcher.startsWith( "maxLen=" ) )
							{
								String[] maxLenStrArr = fvMarcher.split( "=" );
								if ( maxLenStrArr.length == 2 )
								{
									int maxlen = Integer
											.parseInt( maxLenStrArr[1] );
									if ( fvalue.length( ) > maxlen )
									{
										msgList.add( "超过允许最大长度：" + maxlen );
										retVal = false;
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:maxLen设置错误" );
								}
							}
							else if ( fvMarcher.startsWith( "minLen=" ) )
							{
								String[] minLenStrArr = fvMarcher.split( "=" );
								if ( minLenStrArr.length == 2 )
								{
									int minlen = Integer
											.parseInt( minLenStrArr[1] );
									if ( StringUtils.isBlank( fvalue )
											|| fvalue.length( ) < minlen )
									{
										msgList.add( "小于最小长度：" + minlen );
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:minLen设置错误" );
								}
							}
							else if ( fvMarcher.startsWith( "maxInt=" ) )
							{
								String[] maxValStrArr = fvMarcher.split( "=" );
								if ( maxValStrArr.length == 2 )
								{
									try
									{
										Integer.parseInt( maxValStrArr[1] );
									}
									catch ( Exception e )
									{
										log.error( f.getName( )
												+ ":正则表达式有误:maxInt设置错误" );
									}
									if ( StringUtils.isNumeric( fvalue ) )
									{
										int maxVal = Integer
												.parseInt( maxValStrArr[1] );
										if ( fvalue.length( ) > maxVal )
										{
											msgList.add( "大于最大值：" + maxVal );
										}
									}
									else
									{
										msgList.add( "只能输入数值" );
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:minInt设置错误" );
								}
							}
							else if ( fvMarcher.startsWith( "minInt=" ) )
							{
								String[] minValStrArr = fvMarcher.split( "=" );
								if ( minValStrArr.length == 2 )
								{
									try
									{
										Integer.parseInt( minValStrArr[1] );
									}
									catch ( Exception e )
									{
										log.error( f.getName( )
												+ ":正则表达式有误:minInt设置错误" );
									}
									if ( StringUtils.isNumeric( fvalue ) )
									{
										int minVal = Integer
												.parseInt( minValStrArr[1] );
										if ( fvalue.length( ) < minVal )
										{
											msgList.add( "小于最小值：" + minVal );
										}
									}
									else
									{
										msgList.add( "只能输入数值" );
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:minInt设置错误" );
								}
							}
							else
							{
								Pattern pattern = Pattern.compile( fvMarcher );
								Matcher matcher = pattern.matcher( fvalue );
								if ( !matcher.matches( ) )
								{
									msgList.add( "不符合设定格式：" );
								}
							}
						}
						reMap.put( f.getName( ), msgList );
					}
				}
			}
		}
		catch ( Exception e )
		{
			log.error( "", e );
			retVal = false;
		}
		return retVal;
	}

	public Map<String, List<String>> validateBeanObject( T t )
	{
		Map<String, List<String>> reMap = new HashMap<String, List<String>>( );
		boolean retVal = true;
		try
		{
			clazz = t.getClass( );
			Field[] fields = clazz.getDeclaredFields( );
			for ( Field f : fields )
			{
				if ( f.isAnnotationPresent( FormBeanValidate.class ) )
				{
					FormBeanValidate fv = f
							.getAnnotation( FormBeanValidate.class );
					f.setAccessible( true );
					String[] fvMarchers = fv.marcher( );
					if ( fvMarchers != null && fvMarchers.length > 0 )
					{
						List<String> msgList = new ArrayList<String>( );
						String fvalue = null;
						if ( f.get( t ) != null )
						{
							fvalue = f.get( t ).toString( );
						}
						for ( String fvMarcher : fvMarchers )
						{
							if ( fvMarcher.equals( "notNull" ) )
							{
								if ( StringUtils.isBlank( fvalue ) )
								{// 判断空
									msgList.add( "不能为空" );
								}
							}
							if ( StringUtils.isBlank( fvalue ) )
							{// 空值不做判断
								continue;
							}
							if ( fvMarcher.equals( "isIdNum" ) )
							{
								if ( !isIdNum( fvalue ) )
								{
									log.error( f.getName( ) + ":不是有效身份证号码" );
									msgList.add( "不是有效身份证号码" );
								}
							}
							else if ( fvMarcher.equals( "isEmail" ) )
							{
								if ( !isEmail( fvalue ) )
								{
									log.error( f.getName( ) + ":不是有效邮箱" );
									msgList.add( "不是有效邮箱" );
								}
							}
							else if ( fvMarcher.equals( "isNum" ) )
							{
								if ( !StringUtils.isNumeric( fvalue ) )
								{
									log.error( f.getName( ) + ":不是数字" );
								}
							}
							else if ( fvMarcher.startsWith( "maxLen=" ) )
							{
								String[] maxLenStrArr = fvMarcher.split( "=" );
								if ( maxLenStrArr.length == 2 )
								{
									int maxlen = Integer
											.parseInt( maxLenStrArr[1] );
									if ( fvalue.length( ) > maxlen )
									{
										msgList.add( "超过允许最大长度：" + maxlen );
										retVal = false;
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:maxLen设置错误" );
								}
							}
							else if ( fvMarcher.startsWith( "minLen=" ) )
							{
								String[] minLenStrArr = fvMarcher.split( "=" );
								if ( minLenStrArr.length == 2 )
								{
									int minlen = Integer
											.parseInt( minLenStrArr[1] );
									if ( StringUtils.isBlank( fvalue )
											|| fvalue.length( ) < minlen )
									{
										msgList.add( "小于最小长度：" + minlen );
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:minLen设置错误" );
								}
							}
							else if ( fvMarcher.startsWith( "maxInt=" ) )
							{
								String[] maxValStrArr = fvMarcher.split( "=" );
								if ( maxValStrArr.length == 2 )
								{
									try
									{
										Integer.parseInt( maxValStrArr[1] );
									}
									catch ( Exception e )
									{
										log.error( f.getName( )
												+ ":正则表达式有误:maxInt设置错误" );
									}
									if ( StringUtils.isNumeric( fvalue ) )
									{
										int maxVal = Integer
												.parseInt( maxValStrArr[1] );
										if ( fvalue.length( ) > maxVal )
										{
											msgList.add( "大于最大值：" + maxVal );
										}
									}
									else
									{
										msgList.add( "只能输入数值" );
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:minInt设置错误" );
								}
							}
							else if ( fvMarcher.startsWith( "minInt=" ) )
							{
								String[] minValStrArr = fvMarcher.split( "=" );
								if ( minValStrArr.length == 2 )
								{
									try
									{
										Integer.parseInt( minValStrArr[1] );
									}
									catch ( Exception e )
									{
										log.error( f.getName( )
												+ ":正则表达式有误:minInt设置错误" );
									}
									if ( StringUtils.isNumeric( fvalue ) )
									{
										int minVal = Integer
												.parseInt( minValStrArr[1] );
										if ( fvalue.length( ) < minVal )
										{
											msgList.add( "小于最小值：" + minVal );
										}
									}
									else
									{
										msgList.add( "只能输入数值" );
									}
								}
								else
								{
									log.error( f.getName( )
											+ ":正则表达式有误:minInt设置错误" );
								}
							}
							else
							{
								Pattern pattern = Pattern.compile( fvMarcher );
								Matcher matcher = pattern.matcher( fvalue );
								if ( !matcher.matches( ) )
								{
									msgList.add( "不符合设定格式：" );
								}
							}
						}
						reMap.put( f.getName( ), msgList );
					}
				}
			}
		}
		catch ( Exception e )
		{
			log.error( "", e );
		}
		return reMap;
	}

	private static boolean isEmail( String email )
	{
		if ( null == email || "".equals( email ) )
			return false;
		// Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
		Pattern p = Pattern
				.compile( "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*" );// 复杂匹配
		Matcher m = p.matcher( email );
		return m.matches( );
	}

	public boolean isIdNum( String IDStr ) throws ParseException
	{
		if ( StringUtils.isBlank( IDStr ) )
			return true; // 如果为空，不判断，空值必须添加notNull注解明确指出
		String errorInfo = "";// 记录错误信息
		String[] ValCodeArr = {"1", "0", "x", "9", "8", "7", "6", "5", "4",
				"3", "2"};
		String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7",
				"9", "10", "5", "8", "4", "2"};
		String Ai = "";
		// ================ 号码的长度 15位或18位 ================
		if ( IDStr.length( ) != 15 && IDStr.length( ) != 18 )
		{
			errorInfo = "身份证号码长度应该为15位或18位。";
			return false;
		}

		// ================ 数字 除最后以为都为数字 ================
		if ( IDStr.length( ) == 18 )
		{
			Ai = IDStr.substring( 0, 17 );
		}
		else if ( IDStr.length( ) == 15 )
		{
			Ai = IDStr.substring( 0, 6 ) + "19" + IDStr.substring( 6, 15 );
		}
		if ( StringUtils.isNumeric( Ai ) == false )
		{
			errorInfo = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字。";
			return false;
		}

		// ================ 出生年月是否有效 ================
		String strYear = Ai.substring( 6, 10 );// 年份
		String strMonth = Ai.substring( 10, 12 );// 月份
		String strDay = Ai.substring( 12, 14 );// 月份
		if ( isDataFormat( strYear + "-" + strMonth + "-" + strDay,
				"yyyy-MM-dd" ) == false )
		{
			errorInfo = "身份证生日无效。";
			return false;
		}
		GregorianCalendar gc = new GregorianCalendar( );
		SimpleDateFormat s = new SimpleDateFormat( "yyyy-MM-dd" );
		if ( ( gc.get( Calendar.YEAR ) - Integer.parseInt( strYear ) ) > 150
				|| ( gc.getTime( ).getTime( ) - s.parse(
						strYear + "-" + strMonth + "-" + strDay ).getTime( ) ) < 0 )
		{
			errorInfo = "身份证生日不在有效范围。";
			return false;
		}
		if ( Integer.parseInt( strMonth ) > 12
				|| Integer.parseInt( strMonth ) == 0 )
		{
			errorInfo = "身份证月份无效";
			return false;
		}
		if ( Integer.parseInt( strDay ) > 31 || Integer.parseInt( strDay ) == 0 )
		{
			errorInfo = "身份证日期无效";
			return false;
		}

		// ================ 地区码时候有效 ================
		Hashtable<String, String> h = GetAreaCode( );
		if ( h.get( Ai.substring( 0, 2 ) ) == null )
		{
			errorInfo = "身份证地区编码错误。";
			return false;
		}

		// ================ 判断最后一位的值 ================
		int TotalmulAiWi = 0;
		for ( int i = 0; i < 17; i++ )
		{
			TotalmulAiWi = TotalmulAiWi
					+ Integer.parseInt( String.valueOf( Ai.charAt( i ) ) )
					* Integer.parseInt( Wi[i] );
		}
		int modValue = TotalmulAiWi % 11;
		String strVerifyCode = ValCodeArr[modValue];
		Ai = Ai + strVerifyCode;

		if ( IDStr.length( ) == 18 )
		{
			if ( Ai.equals( IDStr ) == false )
			{
				errorInfo = "身份证无效，不是合法的身份证号码";
				return false;
			}
			return true;
		}
		else
		{
			return true;
		}
	}

	private static Hashtable<String, String> GetAreaCode( )
	{
		Hashtable<String, String> hashtable = new Hashtable<String, String>( );
		hashtable.put( "11", "北京" );
		hashtable.put( "12", "天津" );
		hashtable.put( "13", "河北" );
		hashtable.put( "14", "山西" );
		hashtable.put( "15", "内蒙古" );
		hashtable.put( "21", "辽宁" );
		hashtable.put( "22", "吉林" );
		hashtable.put( "23", "黑龙江" );
		hashtable.put( "31", "上海" );
		hashtable.put( "32", "江苏" );
		hashtable.put( "33", "浙江" );
		hashtable.put( "34", "安徽" );
		hashtable.put( "35", "福建" );
		hashtable.put( "36", "江西" );
		hashtable.put( "37", "山东" );
		hashtable.put( "41", "河南" );
		hashtable.put( "42", "湖北" );
		hashtable.put( "43", "湖南" );
		hashtable.put( "44", "广东" );
		hashtable.put( "45", "广西" );
		hashtable.put( "46", "海南" );
		hashtable.put( "50", "重庆" );
		hashtable.put( "51", "四川" );
		hashtable.put( "52", "贵州" );
		hashtable.put( "53", "云南" );
		hashtable.put( "54", "西藏" );
		hashtable.put( "61", "陕西" );
		hashtable.put( "62", "甘肃" );
		hashtable.put( "63", "青海" );
		hashtable.put( "64", "宁夏" );
		hashtable.put( "65", "新疆" );
		hashtable.put( "71", "台湾" );
		hashtable.put( "81", "香港" );
		hashtable.put( "82", "澳门" );
		hashtable.put( "91", "国外" );
		return hashtable;
	}

	public boolean isDataFormat( String str, String format )
	{
		boolean flag = false;
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat( format );
			sdf.setLenient( false );
			sdf.parse( str );
			flag = true;
		}
		catch ( Exception e )
		{
			flag = false;
		}
		return flag;
	}

	public static void main( String[] args )
	{
		SmpDemo sd = new SmpDemo( );
		sd.setDescription( "123" );
		FormBeanValidater<SmpDemo> fv = new FormBeanValidater<SmpDemo>( );
		Map<String, List<String>> reMap = fv.validateBeanObject( sd );
		System.out.println( reMap );
	}

}
