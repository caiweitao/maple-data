package com.caiweitao.data.idworker;

import java.util.Date;

/**
 * @author caiweitao
 * @Date 2021年5月11日
 * @Description long类型全局唯一ID生成器（时间差值+游戏服ID+业务模块ID+序列号），每一个业务模块需创建一个IdWorker对象
 * 相应内容参考https://segmentfault.com/a/1190000011282426
 */
public class LongIdWorker {

	//因为二进制里第一个 bit 为如果是 1，那么都是负数，但是我们生成的 id 都是正数，所以第一个 bit 统一都是 0,剩下63位

	private long serverId;//游戏服ID
	private long moduleId;//业务模块ID
	private long sequence = 0;//序列号，同一毫秒内生成多个id,由序列化递增区分
	private final static long twepoch = 1620731767963L;//起始时间戳，用当前时间戳减去这个时间戳，算出偏移量（一但确定不能再修改）
	protected static long timeBits = 39L;//时间差值占用位数
	protected static long serverIdBits = 10L;//serverId占用的位数
	protected static long moduleIdBits = 6L;//moduleId占用的位数
	protected static long sequenceBits = 8L;//序列号占用的位数
	//-1L ^ (-1L << n)用位运算计算n个bit能表示的最大数值
	private long maxServerId = -1L ^ (-1L << serverIdBits);//serverId可以使用的最大数值
	private long maxModuleId = -1L ^ (-1L << moduleIdBits); //moduleId可以使用的最大数值
	private long maxSequence = -1L ^ (-1L << sequenceBits);//序列号最大值
	private static long moduleIdShift = sequenceBits;//moduleId移位数
	private static long serverIdShift = sequenceBits + moduleIdBits;//serverId移位数
	private static long timestampLeftShift = sequenceBits + moduleIdBits + serverIdBits;//时间差值移位数
	private long lastTimestamp = -1L;//记录产生时间毫秒数，判断是否是同1毫秒
	
	/**
	 * @param serverId 服务器Id
	 * @param moduleId 业务模块id
	 */
	public LongIdWorker(long serverId, long moduleId) {
		// 检查机房id和机器id是否超过31 不能小于0
		if (moduleId > maxModuleId || moduleId < 0) {
			throw new IllegalArgumentException(
					String.format("module Id can't be greater than %d or less than 0",maxModuleId));
		}
		if (serverId > maxServerId || serverId < 0) {
			throw new IllegalArgumentException(
					String.format("server Id can't be greater than %d or less than 0",maxServerId));
		}
		this.moduleId = moduleId;
		this.serverId = serverId;
	}

	/**
	 * 这个是核心方法，通过调用nextId()方法，生成一个全局唯一的id
	 * @return
	 */
	public synchronized long nextId() {
		long timestamp = timeGen();
		if (timestamp < lastTimestamp) {
			System.err.printf(
					"clock is moving backwards. Rejecting requests until %d.", lastTimestamp);
			throw new RuntimeException(
					String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
							lastTimestamp - timestamp));
		}
		// 同一个毫秒内得把seqence序号递增1
		if (lastTimestamp == timestamp) {
			//通过位与运算保证计算的结果范围始终是 0~maxSequence,防止溢出,溢出后归0
			sequence = (sequence + 1) & maxSequence;
			//当某一毫秒的时间，产生的id数 超过maxSequence，系统会进入等待，直到下一毫秒，系统继续产生ID
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0;
		}
		// 记录一下最近一次生成id的时间戳
		lastTimestamp = timestamp;
		// 将对应值移位到对应的位置，拼接起来成一个64 bit的二进制数字，转换成10进制就是个long型
		return ((timestamp - twepoch) << timestampLeftShift) |
				(serverId << serverIdShift) |
				(moduleId << moduleIdShift) | sequence;
	}

	/**
	 * 当某一毫秒的时间，产生的id数超过序列号最大值，系统会进入等待，直到下一毫秒，系统继续产生ID
	 * @param lastTimestamp
	 * @return
	 */
	private long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}
	//获取当前时间戳
	private long timeGen(){
		return System.currentTimeMillis();
	}
	
	/**
	 * 解析id
	 * @param id
	 * @return
	 */
	public static IdInfo analyzeId (long id) {
		IdInfo info = new IdInfo();
		//需要取低几位，就先构建一个低几位全是1，其它位全是0的数据，再与原数据做&运算
		long maxBits = 63;//位数
		//序列号
		long sequenceTemp = Long.MAX_VALUE >> (maxBits-sequenceBits);
		info.setSequence(sequenceTemp & id);
		//业务模块Id
		long notSequenceId = id >> moduleIdShift;//去掉地位sequence剩下的id值
		long moduleTemp = Long.MAX_VALUE >> (maxBits - moduleIdBits);
		info.setModuleId(moduleTemp & notSequenceId);
		//服务器Id
		long notSeqenceModuleId = id >> serverIdShift;
		long serverTemp = Long.MAX_VALUE >> (maxBits - serverIdBits);
		info.setServerId(serverTemp & notSeqenceModuleId);	
		//时间戳
		long notSeqenceModuleServerId = id >> timestampLeftShift;
		long timeTemp = Long.MAX_VALUE >> (maxBits - timeBits);
		long time = timeTemp & notSeqenceModuleServerId;
		info.setTime(time);
		info.setDate(new Date(time + twepoch));
		
		return info;
	}
	
	 //==============================Test=============================================
    /** 测试 */
    public static void main(String[] args) {
        LongIdWorker idWorker = new LongIdWorker(1, 1);
//        long t = System.currentTimeMillis();
//        for (int i = 0; i < 5; i++) {
            long id = idWorker.nextId();
////            System.out.println(Long.toBinaryString(id));
            System.out.println(id);
//            System.out.println(idWorker.analyzeId(id));
//        }
//        System.out.println(System.currentTimeMillis() - t);
//    	System.out.println(-1L ^ (-1L << 39));
//    	System.out.println(Long.MAX_VALUE);
//    	System.out.println(LongIdWorker.analyzeId(1));
    }
    
    /**
	 * @author caiweitao
	 * @Date 2021年5月12日
	 * @Description Id信息
	 */
	public static class IdInfo {
		private long time;//时间差值
		private Date date;//生成id时间
		private long serverId;//服务器Id
		private long moduleId;//业务模块Id
		private long sequence;//序列号
		
		public long getServerId() {
			return serverId;
		}
		public void setServerId(long serverId) {
			this.serverId = serverId;
		}
		public long getModuleId() {
			return moduleId;
		}
		public void setModuleId(long moduleId) {
			this.moduleId = moduleId;
		}
		public long getSequence() {
			return sequence;
		}
		public void setSequence(long sequence) {
			this.sequence = sequence;
		}
		public long getTime() {
			return time;
		}
		public void setTime(long time) {
			this.time = time;
		}
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		@Override
		public String toString() {
			return "IdInfo [timestamp=" + date.getTime() + ", time=" + time + ", date=" + date + ", serverId=" + serverId
					+ ", moduleId=" + moduleId + ", sequence=" + sequence + "]";
		}
		
	}
}
