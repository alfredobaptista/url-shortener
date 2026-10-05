import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 100,
    duration: '30s',

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

const BASE_URL = 'http://localhost:8080';
const SHORT_CODE = 'at8Ovg';

export default function () {
    const response = http.get(
        `${BASE_URL}/${SHORT_CODE}`,
        {
            redirects: 0,
        }
    );

    check(response, {
        'status is 302': (r) => r.status === 302,
        'has Location header': (r) => r.headers['Location'] !== undefined,
    });
}


//k6 run load-tests/redirect-load-test.js