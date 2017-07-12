#!/bin/bash
HOST=localhost
PORT=6379
AUTH=9ERFXpDXvjrVoWhG6IpoZkAz1tMTfkzS60FLswDWTkshB90vDuAffC4D0jP17c
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Events:*" | xargs redis-cli -h localhost -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Proposals:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Proposal:*" | xargs redis-cli -h localhost -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "ApprovedSpeakers:*" | xargs redis-cli -h localhost -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Comments:*" | xargs redis-cli -h localhost -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Leaderboard:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "ApprovedById:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Computed:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "RefusedSpeakers:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Computed:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "TermsAndConditions:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Approved:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Refused:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "ScheduleConfiguration:*" | xargs redis-cli -h $HOST -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "Published:*" | xargs redis-cli -h localhost -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "RefusedById:*" | xargs redis-cli -h localhost -p $PORT -a $AUTH DEL
redis-cli -h $HOST -p $PORT -a $AUTH KEYS "RefusedById:*" | xargs redis-cli -h localhost -p $PORT -a $AUTH DEL
