package cn.com.pyc.sm;

import cn.com.pyc.bean.SmInfo;

/*-
 * 本类统一管理suc的各个位的意义，并综合给出一个直观的值，根据这个值提示相应信息即可
 */
public final class SmRule
{
	private static final int PayMode = 1;			//(int) Math.pow(2, 0);	付费文件

	private static final int CountLimit = 1 << 1;		//(int) Math.pow(2, 1);	次数有限制
	private static final int CountRemain = 1 << 2;		//(int) Math.pow(2, 2);	次数有剩余

	private static final int FreeDateLimit = 1 << 3;		//(int) Math.pow(2, 3);	自由文件，时间有限制
	private static final int FreeDateRemain = 1 << 4;	//(int) Math.pow(2, 4);	自由文件，时间有剩余

	private static final int PayDateLimit = 1 << 5;		//(int) Math.pow(2, 5);	付费文件，时间有限制
	private static final int PayDateRemain = 1 << 6;	//(int) Math.pow(2, 6);	付费文件，时间有剩余

	static final class ReadRule
	{
		public static final int PayDateCount = PayMode | CountLimit
				| CountRemain | PayDateLimit | PayDateRemain;
		public static final int PayCount = PayMode | CountLimit | CountRemain;
		public static final int PayDate = PayMode | PayDateLimit
				| PayDateRemain;
		public static final int FreeDateCount = CountLimit | FreeDateLimit
				| CountRemain | FreeDateRemain;
		public static final int FreeCount = CountLimit | CountRemain;
		public static final int FreeDate = FreeDateLimit | FreeDateRemain;

		private static final int[] ruleToArray()
		{
			return new int[] { PayDateCount, PayCount, PayDate, FreeDateCount,
					FreeCount, FreeDate };
		}
	}

	static final class FinishRule
	{
		public static final int PayDateCount = PayMode | CountLimit
				| PayDateLimit;
		public static final int PayCount = PayMode | CountLimit;
		public static final int PayDate = PayMode | PayDateLimit;
		public static final int FreeDateCount = CountLimit | FreeDateLimit;
		public static final int FreeCount = CountLimit;
		public static final int FreeDate = FreeDateLimit;

		private static final int[] ruleToArray()
		{
			return new int[] { PayDateCount, PayCount, PayDate, FreeDateCount,
					FreeCount, FreeDate };
		}
	}

	static int getReadRule(SmInfo info)
	{
		int[] rules = ReadRule.ruleToArray();
		final int suc = getSuc(info);
		for (int rule : rules)
		{
			if ((suc & rule) == rule)
			{
				return rule;
			}
		}

		return 0;
	}

	static int getFinishRule(SmInfo info)
	{
		int[] rules = FinishRule.ruleToArray();
		final int suc = getSuc(info);
		for (int rule : rules)
		{
			if ((suc & rule) == rule)
			{
				return rule;
			}
		}

		return 0;
	}

	private static int getSuc(SmInfo info)
	{
		int suc = 0;
		if (info.isPayFile())
		{
			suc |= PayMode;
		}

//		if (info.isCountLimit())
//		{
//			suc |= CountLimit;
//		}


		//制作完成界面的显示信息需要用到此判断（例：Ta 无限 天内 能看 18次）
		//在info中加入此处独有的判断方法（次数为0的时候，表示的是 无限次数）
		if (info.isCountUnLimit())
		{
			suc |= CountLimit;
		}



		if (info.isFreeDataLimit())
		{
			suc |= FreeDateLimit;
		}
//		if (info.isPayDataLimit())
//		{
//			suc |= PayDateLimit;
//		}

		//制作完成界面的显示信息需要用到此判断（例：Ta 无限 天内 能看 18次）
		//在info中加入此处独有的判断方法（天数为0的时候，表示的是 无限天数）
		if (info.isPayDaysUnLimit())
		{
			suc |= PayDateLimit;
		}

		//注意这里，打开时次数要减一，可能会是0，但天数是不会的
		if (info.getLeftCount() >= 0)
		{
			suc |= CountRemain;
		}
		if (info.getFreeLeftDays() > 0)
		{
			suc |= FreeDateRemain;
		}
		if ((info.getRemainYears() + info.getRemainDays()) > 0)
		{
			suc |= PayDateRemain;
		}
		return suc;
	}
}
