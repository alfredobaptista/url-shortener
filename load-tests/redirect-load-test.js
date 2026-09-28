import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 100,  //virtual users
    duration: '30s', //test duration

    thresholds: {
        http_req_failed: ['rate<0.01'], // http errors should be less than 1%
        http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    },
};

const SHORT_CODE = 'at8Ovg'

export default function () {
    const response = http.get(
        `http://localhost:8080/${SHORT_CODE}`,
        {
            redirects: 0,
        }
    );

    check(response, {
        'status is 302': (r) => r.status === 302,
    });
}



//k6 run load-tests/redirect-load-test.js