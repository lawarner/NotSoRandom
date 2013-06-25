NotSoRandom
===========

Non-random music shuffle player for Android.

This music player app uses a different manner of classifying music, namely "senses."
Each song is rated by various properties, such as slow/fast, soft/hard,
harmony/dissonance, etc.  Any number of these properties can be defined to classify
songs.  Even with partial values or subjective values, this player offers better than
pure random.

The Sense Value is stored as an int with packed, 4-bit components.  These
components represent one sense, such as tempo.  The range from 0 to 7.  For example,
tempo has a sense value between 0 and 7 for slowest to fastest.  Note the upper bit 
is reserved and must be 0.

Any combination of one or more sense components, even made up ones,  can be used 
to make a sense value.  However the standard definition for a sense value reserves 
the first 3 components as follows:
- tempo (slow / fast)
- roughness (soft / hard)
- taste (sweet / sour)

Other recommended sense components include temperature, humor, length, depth, 
whatever.
