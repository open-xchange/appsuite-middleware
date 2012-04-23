// This file is part of the language categorization Java library.
// Copyright (C) 2005, 2009 Marco Olivo
//
// lc4j is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.olivo.lc4j;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;

/** <p>This class does language categorization by using n-grams to determine the language of the
 * input document.</p>
 *
 * <p>The idea has its roots in <a href="http://odur.let.rug.nl/~vannoord/TextCat/">TextCat</a>,
 * a free Perl library which implements the text categorization algorithm presented in:<br/>
 * <cite>Cavnar, W.&nbsp;B.&nbsp; and J.&nbsp;M.&nbsp;Trenkle, ''N-Gram-Based Text Categorization''
 * In Proceedings of Third Annual Symposium on Document Analysis and Information Retrieval, Las Vegas, NV,
 * UNLV Publications/Reprographics, pp.&nbsp;161-175, 11-13 April 1994</cite>.</p>
 *
 * <p>The paper is currently available at <a href="http://www.novodynamics.com/trenkle/papers/sdair-94-bc.ps.gz">
 * this address</a>.</p>
 *
 * @author Marco Olivo &lt;me@olivo.net&gt;
 * @version $Id$
 */
public class LanguageCategorization {
    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(LanguageCategorization.class));
	/** Enable debugging? */
	private static final boolean DEBUG = LOG.isDebugEnabled();
	/** Default buffer size. */
	private static final int BUFFER_SIZE = 16 * 1024;

	/** Indicates the maximum number of languages to be determined. Above this
	 * threshold, the program will output an <code>UNKNOWN</code> language message.
	 */
	private int MAX_LANGUAGES = 10;
	/** The number of characters to examine in the input. If set to <code>0</code>, all input characters will be used. */
	private int NUM_CHARS_TO_EXAMINE = 1000;
	/** The number of topmost n-grams to use. If set to <code>0</code>, then all n-grams will be used. */
	private int USE_TOPMOST_NGRAMS = 400;
	/** Determines how much worse result must be in order not to be mentioned as an alternative.<br/>
	 * Typical value: <code>1.05</code> or <code>1.1</code>.
	 */
	private float UNKNOWN_THRESHOLD = 1.01f;
	/** The default directory where language-models (<code>.lm</code> files) are in. */
	private String LANGUAGE_MODELS_DIR = "models/";

	/** The array of hash-maps containing the known language-models towards which we compare
	 * the input text language-model.
	 */
	private LanguageModel[] language = null;
	/** The language names. */
	private String[] languageName = null;
	/** The list of word separators used by this class. */
	private final ByteArrayList wordSeparators;

	/** Constructor.
	 */
	public LanguageCategorization() {
		/* prepare a ByteArrayList with word separators (this *MUST* be valid independently of the encoding,
		 * the charset or whatever else. Add a few more here if you want to.
		 */
		wordSeparators = new ByteArrayList();
		wordSeparators.add( (byte)' ' );
		wordSeparators.add( (byte)'\t' );
		wordSeparators.add( (byte)'\r' );
		wordSeparators.add( (byte)'\n' );
		wordSeparators.add( (byte)'0' );
		wordSeparators.add( (byte)'1' );
		wordSeparators.add( (byte)'2' );
		wordSeparators.add( (byte)'3' );
		wordSeparators.add( (byte)'4' );
		wordSeparators.add( (byte)'5' );
		wordSeparators.add( (byte)'6' );
		wordSeparators.add( (byte)'7' );
		wordSeparators.add( (byte)'8' );
		wordSeparators.add( (byte)'9' );
	}

	/** Sets the MAX_LANGUAGES property.
	 *
	 * @param maxLanguages the new value of MAX_LANGUAGES.
	 */
	public void setMaxLanguages( final int maxLanguages ) {
		this.MAX_LANGUAGES = maxLanguages;
	}

	/** Sets the NUM_CHARS_TO_EXAMINE property.
	 *
	 * @param numCharsToExamine the new value of NUM_CHARS_TO_EXAMINE.
	 */
	public void setNumCharsToExamine( final int numCharsToExamine ) {
		this.NUM_CHARS_TO_EXAMINE = numCharsToExamine;
	}

	/** Sets the USE_TOPMOST_NGRAMS property.
	 *
	 * @param useTopmostNgrams the new value of USE_TOPMOST_NGRAMS.
	 */
	public void setUseTopmostNgrams( final int useTopmostNgrams ) {
		this.USE_TOPMOST_NGRAMS = useTopmostNgrams;
	}

	/** Sets the UNKNOWN_THRESHOLD property.
	 *
	 * @param unknownThreshold the new value of UNKNOWN_THRESHOLD.
	 */
	public void setUnknownThreshold( final float unknownThreshold ) {
		this.UNKNOWN_THRESHOLD = unknownThreshold;
	}

	/** Sets the LANGUAGE_MODELS_DIR property.
	 *
	 * @param languageModelsDir the new value of LANGUAGE_MODELS_DIR.
	 */
	public void setLanguageModelsDir( final String languageModelsDir ) {
		this.LANGUAGE_MODELS_DIR = languageModelsDir;
	}

	/** Gets the MAX_LANGUAGES property.
	 *
	 * @return the MAX_LANGUAGES property.
	 */
	public int getMaxLanguages() {
		return this.MAX_LANGUAGES;
	}

	/** Gets the NUM_CHARS_TO_EXAMINE property.
	 *
	 * @return the NUM_CHARS_TO_EXAMINE property.
	 */
	public int getNumCharsToExamine() {
		return this.NUM_CHARS_TO_EXAMINE;
	}

	/** Gets the USE_TOPMOST_NGRAMS property.
	 *
	 * @return the USE_TOPMOST_NGRAMS property.
	 */
	public int getUseTopmostNgrams() {
		return this.USE_TOPMOST_NGRAMS;
	}

	/** Gets the UNKNOWN_THRESHOLD property.
	 *
	 * @return the UNKNOWN_THRESHOLD propery.
	 */
	public float getUnknownThreshold() {
		return this.UNKNOWN_THRESHOLD;
	}

	/** Gets the LANGUAGE_MODELS_DIR property.
	 *
	 * @return the LANGUAGE_MODELS_DIR property.
	 */
	public String getLanguageModelsDir() {
		return this.LANGUAGE_MODELS_DIR;
	}

	/** <p>Creates a {@link LanguageModel} object from the given input text.</p>
	 *
	 * <p><strong>BE WARNED THAT</strong> good results are obtained by passing to this method
	 * a full text, together with numbers, punctuation and other text characters. So, if you
	 * have - say - HTML, just throw away tags, but leave the rest if you want to obtain
	 * precise results: punctuation comes in very handy at determining the language.<br/>
	 * At some extent, also upper/lower case letters could help.</p>
	 *
	 * @param input the text upon which the language-model should be built.
	 * @return the language-model resulting from the given input text.
	 */
	public LanguageModel createLanguageModel( final ByteArrayList input ) {
	    final long startTime = DEBUG ? System.currentTimeMillis() : 0L;

		final IncrementalInt2IntMap hash = new IncrementalInt2IntMap();
		final LanguageModel languageModel = new LanguageModel();

		// append a trailing space so that we consider also the last word
		input.add( (byte)' ' );

		final ByteArrayList word = new ByteArrayList();
		word.add( (byte)'_' );
		final int m = ( this.NUM_CHARS_TO_EXAMINE > 0 ? Math.min( input.size(), this.NUM_CHARS_TO_EXAMINE ) : input.size() );

		byte[] x;
		for ( int i = 0; i < m; i++ ) {
			final byte b = input.getByte( i );
			if ( wordSeparators.indexOf( b ) == -1 ) {
				word.add( b );
				continue;
			}
			else {
				word.add( (byte)'_' );
			}

			final int wordLength = word.size();
			int length = wordLength;

			if ( DEBUG ) {
                LOG.error( "parsing " + word.toString() );
            }

			for ( int k = 0; k < wordLength; k++ ) {
				x = word.elements();
				if ( length > 4 ) {
					hash.inc( ( new ByteArrayList( x, k, 5 ) ).hashCode(), 1 );
				}
				if ( length > 3 ) {
					hash.inc( ( new ByteArrayList( x, k, 4 ) ).hashCode(), 1 );
				}
				if ( length > 2 ) {
					hash.inc( ( new ByteArrayList( x, k, 3 ) ).hashCode(), 1 );
				}
				if ( length > 1 ) {
					hash.inc( ( new ByteArrayList( x, k, 2 ) ).hashCode(), 1 );
				}
				hash.inc( ( new ByteArrayList( x, k, 1 ) ).hashCode(), 1 );

				length--;
			}

			word.clear();
			word.add( (byte)'_' );
		}

		if ( DEBUG ) {
            LOG.error( "hash size is " + hash.size() );
        }

		final int[] ngrams = hash.getOrderedKeysByScore();
		final int n = ( this.USE_TOPMOST_NGRAMS > 0 ? Math.min( ngrams.length, this.USE_TOPMOST_NGRAMS ) : ngrams.length );
		for ( int k = 0; k < n; k++ ) {
			try {
				languageModel.add( ngrams[k], hash.get( ngrams[k] ) );
			}
			catch ( final IllegalArgumentException e ) {
				LOG.error( "WARNING: resulting language-model will be very likely invalid!", e );
				break;
			}

			if ( DEBUG ) {
                LOG.debug( "adding " + ngrams[k] + " with weight " + hash.get( ngrams[k] ) + " with pos " + k + " to languageModel" );
            }
		}

		if ( DEBUG ) {
            LOG.debug( "size of languageModel: " + languageModel.size() );
        }

		if ( DEBUG ) {
    		final long endTime = System.currentTimeMillis();
    		LOG.debug( "time taken to create language-model from input: " + (double)( endTime - startTime ) / 1000 + "s" );
		}

		return languageModel;
	}

	/** Computes the distance between the two given language-models.
	 *
	 * @param lang1 the first language-model.
	 * @param lang2 the second language-model.
	 * @return the distance between the two language-models.
	 */
	public int calcDistance( final LanguageModel lang1, final LanguageModel lang2 ) {
		int distance = 0;

		int i = 0;
		int x;
		final int n = lang1.size();
		while ( i < n ) {
			final int val = lang1.getNgram( i );
			if ( DEBUG ) {
                LOG.debug( "checking " + val );
            }

			x = lang2.getPos( val );

			if ( x != -1 ) {
                distance += Math.abs( x - i );
            } else {
                distance += this.USE_TOPMOST_NGRAMS;
            }

			i++;
		}

		return distance;
	}

	/** Loads languages in this object's structures.
	 *
	 * @param path the path where language-model files are.
	 * @throws IOException if some IO error occurs.
	 * @throws FileNotFoundException if the directory in which files are supposed to be cannot be found.
	 */
	public void loadLanguages( final String path ) throws IOException, FileNotFoundException {
		if ( language == null ) {	// load language-models only if not already loaded
		    final long startTime = DEBUG ? System.currentTimeMillis() : 0L;

			if ( DEBUG ) {
                LOG.debug( "loading language-models from files in " + path );
            }

			final File[] files = new File( path ).listFiles();
			final int n = files.length;
			language = new LanguageModel[n];
			languageName = new String[n];

			if ( n == 0 ) {
				LOG.warn( "WARNING: no language-model files were found in the specified path (" + path + "). Please check." );
			}

			if ( DEBUG ) {
                LOG.debug( "found " + n + " language-model files" );
            }

			for ( int i = 0; i < n; i++ ) {
				final String s;
				int k = 0;

				language[i] = new LanguageModel();
				languageName[i] = files[i].getName();

				// read language-model from file
				final DataInputStream dis = new DataInputStream( new FastBufferedInputStream( new FileInputStream( files[i] ), BUFFER_SIZE ) );
				while ( k < this.USE_TOPMOST_NGRAMS ) {
					try {
						final int input = dis.readInt();
						final int ngramFreq = dis.readInt();

						language[i].add( input, ngramFreq );
					} catch ( final EOFException e ) {
						break;
					} catch ( final IllegalArgumentException e ) {
						LOG.error( e );
						break;
					}

					k++;
				}
				dis.close();
			}

			if (DEBUG) {
                final long endTime = System.currentTimeMillis();
                LOG.debug("time taken to load all available language-models: " + (double) (endTime - startTime) / 1000 + "s");
            }
		}

		if ( language == null || language.length == 0 ) {
			LOG.warn( "No language-model loaded." );
			return;
		}

		if ( DEBUG ) {
            LOG.debug( "language-model files loaded" );
        }
	}

	/** Finds the language in which the input string is most likely written. Returns a {@link List}
	 * of languages (up to MAX_LANGUAGES) in which the input string is probably written. If there are
	 * more than MAX_LANGUAGES occurrences for an input, the only element in the list is the
	 * <code>UNKNOWN</code> {@link String}.
	 *
	 * @param input the {@link ByteArrayList} representing the input text (in bytes).
	 * @return a {@link List} containing the most likely language(s) in which the input string
	 *		   is written. If an error occurrs, <code>null</code> is returned.
	 */
	public List<String> findLanguage( final ByteArrayList input ) {
		final List<String> ret = new LinkedList<String>();

		// create input language-model
		final LanguageModel inputLM = createLanguageModel( input );

		// load language-models from language-model files into an array of hash-maps
		try {
			this.loadLanguages( this.LANGUAGE_MODELS_DIR );
		} catch ( final Exception e ) {
			LOG.error( "An exception was thrown when trying to load languages. Returning null.", e );
			return null;
		}

		final long startTime = DEBUG ? System.currentTimeMillis() : 0L;

		// where we store probabilities and language indexes
		final int n = language.length;
		final int[] prob = new int[n];
		final int[] langIndex = new int[n];

		// compare the input language-model with each language-model extracted from files
		for ( int i = 0; i < n; i++ ) {
			prob[i] = calcDistance( inputLM, language[i] );
			langIndex[i] = i;
		}

		// find out the most likely language(s), if any, by comparing the distance from each of the language-models
		final IntComparator comp = new IntComparator() {
								@Override
                                public int compare( final int i, final int j ) {
									if ( prob[ i ] > prob[ j ] ) {
                                        return 1;
                                    }
									if ( prob[ i ] < prob[ j ] ) {
                                        return -1;
                                    }
									return 0;
								}
							};
		final Swapper swapper = new Swapper() {
								@Override
                                public void swap( final int i, final int j ) {
									final int t = prob[ i ];
									prob[ i ] = prob[ j ];
									prob[ j ] = t;

									final int u = langIndex[ i ];
									langIndex[ i ] = langIndex[ j ];
									langIndex[ j ] = u;
								}
							};
		GenericSorting.mergeSort( 0, n, comp, swapper );

		final int maxProb = prob[0];
		int countAnswers = 0;

		if ( DEBUG ) {
            for ( int i = 0; i < n; i++ ) {
                LOG.debug( "lang=" + languageName[langIndex[i]] + " prob=" + prob[i] );
            }
        }
		if ( DEBUG ) {
            LOG.debug( "maxProb=" + maxProb );
        }

		for ( int i = 0; i < n; i++ ) {
			if ( prob[i] < this.UNKNOWN_THRESHOLD * maxProb || prob[i] == 0 ) {
				/* prob[i] == 0 in the check above is for the (almost impossible) case that the input language-model
				 * corresponds exactly to the original language-model. This could tipically happen only if the input
				 * language-model is the same used for training.
				 */
				countAnswers++;
				ret.add( languageName[langIndex[i]] );
			} else {
                break;
            }
		}
		if ( DEBUG ) {
            LOG.debug( "countAnswers=" + countAnswers );
        }

		if ( countAnswers > this.MAX_LANGUAGES ) {
			ret.clear();
			ret.add( "UNKNOWN" );
		}

		if (DEBUG) {
            final long endTime = System.currentTimeMillis();
            LOG.debug("time taken to effectively determine the language: " + (double) (endTime - startTime) / 1000 + "s");
        }

        return ret;
	}

	/** The main loop.
	 *
	 * @param args the arguments.
	 * @throws IOException if some IO error occurs.
	 */
	public static void main( final String args[] ) throws IOException {
		int ch;
		boolean createNewLanguage = false;
		String newLanguageName = null;
		final LanguageCategorization lc = new LanguageCategorization();

		final LongOpt[] longopts = new LongOpt[] {
				new LongOpt( "help", LongOpt.NO_ARGUMENT, null, 'h' ),
				new LongOpt( "max-languages", LongOpt.REQUIRED_ARGUMENT, null, 'm' ),
				new LongOpt( "num-chars-to-examine", LongOpt.REQUIRED_ARGUMENT, null, 'n' ),
				new LongOpt( "use-topmost-ngrams", LongOpt.REQUIRED_ARGUMENT, null, 't' ),
				new LongOpt( "unknown-threshold", LongOpt.REQUIRED_ARGUMENT, null, 'u' ),
				new LongOpt( "languageModel-dir", LongOpt.REQUIRED_ARGUMENT, null, 'd' ),
				new LongOpt( "create-new-languageModel", LongOpt.NO_ARGUMENT, null, 'c' )
		};

		final Getopt g = new Getopt( "LanguageCategorization", args, "m:n:u:n:t:d:ch", longopts );
		g.setOpterr( true );

		while ( ( ch = g.getopt() ) != -1 ) {
            switch ( ch ) {
            	case 'h':
            		System.err.println( "Usage: LanguageCategorization [OPTIONS]" );
            		System.err.println( "Determines the language in which stdin text is written." );
            		System.err.println( "" );
            		System.err.println( "Optional arguments:" );
            		System.err.println( "  -m, --max-languages            the maximum number of languages to be determined (default: " + lc.getMaxLanguages() + ")" );
            		System.err.println( "  -n, --num-chars-to-examine     the number of characters to examine in the input (default: " + lc.getNumCharsToExamine() + ")" );
            		System.err.println( "  -t, --use-topmost-ngrams       forces the usage of n-grams up to this length (default: any length)" );
            		System.err.println( "  -u, --unknown-threshold        determines how much worse result must be in order not to be mentioned as an alternative (default: " + lc.getUnknownThreshold() + ")" );
            		System.err.println( "  -d, --languageModel-dir        use the given folder as the directory where to store/retrieve language-model files" );
            		System.err.println( "  -c, --create-new-languageModel creates a new language-model using the input text. The argument value is used as the name for the new language. Output goes to stdout" );
            		System.err.println( "" );
            		System.err.println( "Help:" );
            		System.err.println( "  -h, --help                     print this help screen" );
            		System.err.println( "" );
            		return;

            	case 'm':
            		lc.setMaxLanguages( Integer.parseInt( g.getOptarg() ) );
            		break;

            	case 'n':
            		lc.setNumCharsToExamine( Integer.parseInt( g.getOptarg() ) );
            		break;

            	case 't':
            		lc.setUseTopmostNgrams( Integer.parseInt( g.getOptarg() ) );
            		break;

            	case 'u':
            		lc.setUnknownThreshold( Float.parseFloat( g.getOptarg() ) );
            		break;

            	case 'd':
            		lc.setLanguageModelsDir( g.getOptarg() );
            		break;

            	case 'c':
            		createNewLanguage = true;
            		newLanguageName = g.getOptarg();
            		break;

            	case '?':
            		return;

            	default:
            		break;
            }
        }

		// read from stdin
		final ByteArrayList input = new ByteArrayList();
		final DataInputStream dis = new DataInputStream( new FastBufferedInputStream( System.in, BUFFER_SIZE ) );
		try {
			while ( true ) {
                input.add( dis.readByte() );
            }
		}
		catch ( final EOFException e ) {
			dis.close();
		}

		if ( createNewLanguage ) {
			// create input language-model...
			final LanguageModel languageModel = lc.createLanguageModel( input );

			// ...and send it to stdout
			final DataOutputStream dos = new DataOutputStream( new FastBufferedOutputStream( System.out, BUFFER_SIZE ) );
			for ( int i = 0; i < languageModel.size(); i++ ) {
				final int ngram = languageModel.getNgram( i );
				final int freq = languageModel.getFreq( i );

				dos.writeInt( ngram );
				dos.writeInt( freq );
			}
			dos.close();
		}
		else {
			System.out.println( "probable language(s): " + lc.findLanguage( input ) );
		}
	}
}

// Local Variables:
// mode: jde
// tab-width: 4
// End:
