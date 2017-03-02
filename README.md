# snap
Given a ego network of a Facebook user, find the social circles a new friend belongs to

### Paper
* https://cs.stanford.edu/people/jure/pubs/circles-tkdd14.pdf
* https://cs.stanford.edu/people/jure/pubs/circles-nips12.pdf

### Dataset
http://snap.stanford.edu/data/

### Algorithm
Goal is to predict the social cirlces to which a new facebook friend belongs to
* Compute alpha and theta which explains the existing friends and their circles
 * Use gradient ascent with log likelihood function as cost
* Assign the new friend to all combinations of circles and keep the assignment that gives the maximum log likelihood

### Evaluation
To evaluate, we use leave one out methodology.
* Precision = ~24%
* Recall = ~86%
* Accuracy = ~72%

### To run
`mvn compile exec:java`

### Tools needed
* JDK
* Maven `sudo apt-get install maven`
