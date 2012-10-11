package me.prettyprint.cassandra.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;

/**
 * @author: peter
 */
public class BasicColumnFamilyDefinition implements ColumnFamilyDefinition {


  private String keyspaceName;
  private String name;
  private ColumnType columnType = ColumnType.STANDARD;
  private ComparatorType comparatorType = ComparatorType.BYTESTYPE;
  private ComparatorType subComparatorType;
	private String comparatorTypeAlias = "";
	private String subComparatorTypeAlias = "";
  private String comment;
  private double rowCacheSize;
  private double keyCacheSize;
  private double readRepairChance;
  private int gcGraceSeconds;
  private String defaultValidationClass;
  private String keyValidationClass;
  private int id;
  private int maxCompactionThreshold;
  private int minCompactionThreshold;
  private int rowCacheSavePeriodInSeconds;
  private int keyCacheSavePeriodInSeconds;
  private double memtableOperationsInMillions;
  private int memtableThroughputInMb;
  private int memtableFlushAfterMins;
  private boolean replicateOnWrite;

  private String compactionStrategy;
  private Map<String,String> compactionStrategyOptions;
  private Map<String,String> compressionOptions;
  private double mergeShardsChance;
  private String rowCacheProvider;
  private ByteBuffer keyAlias;
  private int rowCacheKeysToSave;


  private final List<ColumnDefinition> columnDefinitions;


  public BasicColumnFamilyDefinition() {
    this.columnDefinitions = new ArrayList<ColumnDefinition>();
  }

  /**
   * Builds a {@link BasicColumnFamilyDefinition} based off the interface
   */
  public BasicColumnFamilyDefinition(ColumnFamilyDefinition columnFamilyDefinition) {
    keyspaceName = columnFamilyDefinition.getKeyspaceName();
    name = columnFamilyDefinition.getName();
    columnType = columnFamilyDefinition.getColumnType();
    comparatorType = columnFamilyDefinition.getComparatorType();
    subComparatorType = columnFamilyDefinition.getSubComparatorType();
		comparatorTypeAlias = columnFamilyDefinition.getComparatorTypeAlias();
		subComparatorTypeAlias = columnFamilyDefinition.getSubComparatorTypeAlias();
    comment = columnFamilyDefinition.getComment();
    rowCacheSize = columnFamilyDefinition.getRowCacheSize();
    rowCacheSavePeriodInSeconds = columnFamilyDefinition.getRowCacheSavePeriodInSeconds();
    keyCacheSize = columnFamilyDefinition.getKeyCacheSize();
    keyCacheSavePeriodInSeconds = columnFamilyDefinition.getKeyCacheSavePeriodInSeconds();
    readRepairChance = columnFamilyDefinition.getReadRepairChance();
    columnDefinitions = columnFamilyDefinition.getColumnMetadata() != null
    ? new ArrayList<ColumnDefinition>(columnFamilyDefinition.getColumnMetadata())
        : new ArrayList<ColumnDefinition>();
    gcGraceSeconds = columnFamilyDefinition.getGcGraceSeconds();
    defaultValidationClass = columnFamilyDefinition.getDefaultValidationClass();
    keyValidationClass = columnFamilyDefinition.getKeyValidationClass();
    id = columnFamilyDefinition.getId();
    minCompactionThreshold = columnFamilyDefinition.getMinCompactionThreshold();
    maxCompactionThreshold = columnFamilyDefinition.getMaxCompactionThreshold();
    memtableOperationsInMillions = columnFamilyDefinition.getMemtableOperationsInMillions();
    memtableThroughputInMb = columnFamilyDefinition.getMemtableThroughputInMb();
    memtableFlushAfterMins = columnFamilyDefinition.getMemtableFlushAfterMins();
    replicateOnWrite = columnFamilyDefinition.isReplicateOnWrite();
    compactionStrategy = columnFamilyDefinition.getCompactionStrategy();
    compactionStrategyOptions = columnFamilyDefinition.getCompactionStrategyOptions();
    compressionOptions = columnFamilyDefinition.getCompressionOptions();
    mergeShardsChance = columnFamilyDefinition.getMergeShardsChance();
    rowCacheProvider = columnFamilyDefinition.getRowCacheProvider();
    keyAlias = columnFamilyDefinition.getKeyAlias();
    rowCacheKeysToSave = columnFamilyDefinition.getRowCacheKeysToSave();

  }

  @Override
public void setKeyspaceName(String keyspaceName) {
    this.keyspaceName = keyspaceName;
  }

  @Override
public void setName(String name) {
    this.name = name;
  }

  @Override
public void setColumnType(ColumnType columnType) {
    this.columnType = columnType;
  }

  @Override
public void setComparatorType(ComparatorType comparitorType) {
    this.comparatorType = comparitorType;
  }

  @Override
public void setSubComparatorType(ComparatorType subComparitorType) {
    this.subComparatorType = subComparitorType;
  }

	@Override
    public void setComparatorTypeAlias(String alias) { this.comparatorTypeAlias = alias; }

	@Override
    public void setSubComparatorTypeAlias(String alias) { this.subComparatorTypeAlias = alias; }

  @Override
public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
public void setRowCacheSize(double rowCacheSize) {
    this.rowCacheSize = rowCacheSize;
  }

  @Override
public void setKeyCacheSize(double keyCacheSize) {
    this.keyCacheSize = keyCacheSize;
  }

  @Override
public void setReadRepairChance(double readRepairChance) {
    this.readRepairChance = readRepairChance;
  }

  @Override
public void setGcGraceSeconds(int gcGraceSeconds) {
    this.gcGraceSeconds = gcGraceSeconds;
  }

  @Override
public void setDefaultValidationClass(String defaultValidationClass) {
    this.defaultValidationClass = defaultValidationClass;
  }

  @Override
public void setId(int id) {
    this.id = id;
  }

  @Override
public void setMaxCompactionThreshold(int maxCompactionThreshold) {
    this.maxCompactionThreshold = maxCompactionThreshold;
  }

  @Override
public void setMinCompactionThreshold(int minCompactionThreshold) {
    this.minCompactionThreshold = minCompactionThreshold;
  }

  @Override
public void setRowCacheSavePeriodInSeconds(int rowCacheSavePeriodInSeconds) {
    this.rowCacheSavePeriodInSeconds = rowCacheSavePeriodInSeconds;
  }

  @Override
public void setMemtableOperationsInMillions(double memtableOperationsInMillions) {
    this.memtableOperationsInMillions = memtableOperationsInMillions;
  }

  @Override
public void setMemtableThroughputInMb(int memtableThroughputInMb) {
    this.memtableThroughputInMb = memtableThroughputInMb;
  }

  @Override
public void setMemtableFlushAfterMins(int memtableFlushAfterMins) {
    this.memtableFlushAfterMins = memtableFlushAfterMins;
  }

  @Override
public void setReplicateOnWrite(boolean replicateOnWrite) {
    this.replicateOnWrite = replicateOnWrite;
  }

  @Override
public void addColumnDefinition( ColumnDefinition columnDefinition){
    this.columnDefinitions.add( columnDefinition );
  }

  @Override
public void setKeyCacheSavePeriodInSeconds(int keyCacheSavePeriodInSeconds) {
    this.keyCacheSavePeriodInSeconds = keyCacheSavePeriodInSeconds;
  }

  @Override
public void setKeyValidationClass(String keyValidationClass){
      this.keyValidationClass = keyValidationClass;
  }

  /**
   * SHOULD THIS BE HERE? A COLUMN DEFINITION IS PART OF A KEYSPACE BY VIRTUE
   * OF BEING IN A KEYSPACE LIST
   */
  @Override
  public String getKeyspaceName() {
    return this.keyspaceName;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public ColumnType getColumnType() {
    return this.columnType;
  }

  @Override
  public ComparatorType getComparatorType() {
    return this.comparatorType;
  }

  @Override
  public ComparatorType getSubComparatorType() {
    return this.subComparatorType;
  }

	@Override
    public String getComparatorTypeAlias() { return this.comparatorTypeAlias; }

	@Override
    public String getSubComparatorTypeAlias() { return this.subComparatorTypeAlias; }

  @Override
  public String getComment() {
    return this.comment;
  }

  @Override
  public double getRowCacheSize() {
    return this.rowCacheSize;
  }

  @Override
  public int getRowCacheSavePeriodInSeconds() {
    return this.rowCacheSavePeriodInSeconds;
  }

  @Override
  public double getKeyCacheSize() {
    return this.keyCacheSize;
  }

  @Override
  public double getReadRepairChance() {
    return this.readRepairChance;
  }

  @Override
  public List<ColumnDefinition> getColumnMetadata() {
    return this.columnDefinitions;
  }

  @Override
  public int getGcGraceSeconds() {
    return this.gcGraceSeconds;
  }

  @Override
  public String getDefaultValidationClass() {
    return this.defaultValidationClass;
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public int getMaxCompactionThreshold() {
    return this.maxCompactionThreshold;
  }

  @Override
  public int getMinCompactionThreshold() {
    return this.minCompactionThreshold;
  }

  @Override
  public int getMemtableFlushAfterMins() {
    return this.memtableFlushAfterMins;
  }

  @Override
  public double getMemtableOperationsInMillions() {
    return this.memtableOperationsInMillions;
  }

  @Override
  public int getMemtableThroughputInMb() {
    return this.memtableThroughputInMb;
  }

  @Override
public boolean isReplicateOnWrite() {
    return replicateOnWrite;
  }

  @Override
public int getKeyCacheSavePeriodInSeconds() {
    return keyCacheSavePeriodInSeconds;
  }

  @Override
  public String getKeyValidationClass() {
      return keyValidationClass;
  }

  @Override
  public String getCompactionStrategy() {
    return compactionStrategy;
  }

  @Override
  public void setCompactionStrategy(String compactionStrategy) {
    this.compactionStrategy = compactionStrategy;
  }

  @Override
  public Map<String, String> getCompactionStrategyOptions() {
    return compactionStrategyOptions;
  }

  @Override
  public void setCompactionStrategyOptions(Map<String, String> compactionStrategyOptions) {
    this.compactionStrategyOptions = compactionStrategyOptions;
  }

  @Override
  public Map<String, String> getCompressionOptions() {
    return compressionOptions;
  }

  @Override
  public void setCompressionOptions(Map<String, String> compressionOptions) {
    this.compressionOptions = compressionOptions;
  }

  @Override
public double getMergeShardsChance() {
    return mergeShardsChance;
  }

  @Override
public void setMergeShardsChance(double mergeShardsChance) {
    this.mergeShardsChance = mergeShardsChance;
  }

  @Override
public String getRowCacheProvider() {
    return rowCacheProvider;
  }

  @Override
public void setRowCacheProvider(String rowCacheProvider) {
    this.rowCacheProvider = rowCacheProvider;
  }

  @Override
public ByteBuffer getKeyAlias() {
    return keyAlias;
  }

  @Override
public void setKeyAlias(ByteBuffer keyAlias) {
    this.keyAlias = keyAlias;
  }

  @Override
public int getRowCacheKeysToSave() {
    return rowCacheKeysToSave;
  }

  @Override
public void setRowCacheKeysToSave(int rowCacheKeysToSave) {
    this.rowCacheKeysToSave = rowCacheKeysToSave;
  }
}
