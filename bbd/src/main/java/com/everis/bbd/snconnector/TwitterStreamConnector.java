package com.everis.bbd.snconnector;

import java.util.List;
import java.util.logging.Logger;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 */
public class TwitterStreamConnector extends AbstractTwitterConnector 
{
	/**
	 * Logger.
	 */
	protected static Logger log = Logger.getLogger(TwitterStreamConnector.class.getName());

	/**
	 * Twitter instance for streaming.
	 */
	private TwitterStream _twitter;

	private FilterQuery _twitterQuery;
	
	private List<String> _tracks;
	
	/**
	 * Default configuration file constructor.
	 */
	public TwitterStreamConnector()
	{
		this(DEFAULT_CONFIGURATION_PATH);
	}

	/**
	 * Returns a TwitterStreamConnector configured with the properties in
	 * propertiesFile.
	 * 
	 * @param propertiesFile file path with the properties (tokens).
	 */
	public TwitterStreamConnector(String propertiesFile) 
	{
		super(propertiesFile);
	}

	/**
	 * Initializes the configuration and the results.
	 * Also configures the query.
	 */
	@Override
	public boolean configure(String propertiesFile)
	{
		if (super.configure(propertiesFile))
		{
			_twitterQuery = new FilterQuery();
			
			if (_configuration.exists(TwitterConnectorKeys.CONF_QUERY_KEY.getId()) > 0)
			{
				_tracks = _configuration.getValues(TwitterConnectorKeys.CONF_QUERY_KEY.getId());
				String[] tracks = _tracks.toArray(new String[_tracks.size()]);
				_twitterQuery.track(tracks);
			}
			else
			{
				log.severe("Query not specified in file "+_propertiesFile+".");
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean connectToTwitter(ConfigurationBuilder cb)
	{
		TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
		_twitter = tf.getInstance();
		return true;
	}

	@Override
	public void close() 
	{
		_twitter.shutdown();
		_configuration.clearConfiguration();
		clearResults();
	}

	@Override
	public int query(boolean appendResults) 
	{
		if(!appendResults)
		{
			clearResults();
		}
		
		StatusListener listener = new StatusListener() 
		{
			public void onStatus(Status status) 
			{
				log.info("New event received.");
				synchronized(_results)
				{
					_results.add(statusToJSONObject(status, _search));
				}
			}
			
			// This listener will ignore everything except for new tweets
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
			public void onScrubGeo(long userId, long upToStatusId) {}
			public void onException(Exception ex) 
			{
				ex.printStackTrace();
			}

			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		_twitter.addListener(listener);
		
		log.info("Starting to sample tweets.");
		_twitter.filter(_twitterQuery);
		return 0;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public int query(String query, boolean appendResults) 
	{
		return 0;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public int nextQuery() 
	{
		return 0;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public boolean hasNextQuery() 
	{
		return false;
	}

}
